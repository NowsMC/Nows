/*
 * Copyright 2026 TamKungZ_ (Nows MC — https://nows.space)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.nows.mcnows.core.classloading;

import space.nows.mcnows.api.ClassTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Stable class-loading kernel. Integration-specific parent-first packages are
 * configured by runtime composition rather than hard-coded here.
 */
public final class NowsClassLoader extends URLClassLoader {
    static { registerAsParallelCapable(); }

    @FunctionalInterface
    public interface ClassGenerator {
        byte[] generate(String className) throws Exception;
    }

    private final CopyOnWriteArrayList<ClassTransformer> transformers = new CopyOnWriteArrayList<>();
    private final Set<String> parentFirstPrefixes = ConcurrentHashMap.newKeySet();
    private final Set<String> childFirstPrefixes = ConcurrentHashMap.newKeySet();
    private final Set<String> childOnlyPrefixes = ConcurrentHashMap.newKeySet();
    private volatile ClassGenerator classGenerator;

    public NowsClassLoader(URL[] urls, ClassLoader parent) {
        super("nows-game", urls, parent);
        parentFirstPrefixes.addAll(List.of("java.", "javax.", "jakarta.", "jdk.", "sun."));
    }

    public void addParentFirstPrefix(String prefix) { parentFirstPrefixes.add(prefix); }
    public void addChildFirstPrefix(String prefix) { childFirstPrefixes.add(prefix); }
    public void addChildOnlyPrefix(String prefix) { childOnlyPrefixes.add(prefix); }
    public void addTransformer(ClassTransformer transformer) { transformers.add(transformer); }
    public void addTransformerFirst(ClassTransformer transformer) { transformers.add(0, transformer); }
    public void setClassGenerator(ClassGenerator generator) { classGenerator = generator; }
    public boolean isClassLoaded(String className) { return findLoadedClass(className) != null; }

    public byte[] getRawClassBytes(String className) throws IOException {
        String resource = className.replace('.', '/') + ".class";
        URL own = findResource(resource);
        if (own != null) {
            try (InputStream input = own.openStream()) { return input.readAllBytes(); }
        }
        try (InputStream input = getParent().getResourceAsStream(resource)) {
            return input == null ? null : input.readAllBytes();
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded == null && !isParentFirst(name)) {
                try { loaded = findClass(name); } catch (ClassNotFoundException ignored) { }
            }
            if (loaded == null && isChildOnly(name)) throw new ClassNotFoundException(name);
            if (loaded == null) loaded = super.loadClass(name, false);
            if (resolve) resolveClass(loaded);
            return loaded;
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            ClassData classData = getOwnClassData(name);
            byte[] bytes = classData == null ? null : classData.bytes();
            if (bytes == null && classGenerator != null) bytes = classGenerator.generate(name);
            if (bytes == null) throw new ClassNotFoundException(name);
            for (ClassTransformer transformer : List.copyOf(transformers)) {
                bytes = transformer.transform(name, bytes);
                if (bytes == null) throw new ClassNotFoundException("Transformer returned null for " + name);
            }
            if (classData == null) {
                return defineClass(name, bytes, 0, bytes.length);
            }
            CodeSource codeSource = new CodeSource(classData.codeSource(), (Certificate[]) null);
            return defineClass(name, bytes, 0, bytes.length, codeSource);
        } catch (ClassNotFoundException e) {
            throw e;
        } catch (Throwable t) {
            LinkageError failure = new LinkageError("Failed to define " + name);
            failure.initCause(t);
            throw failure;
        }
    }

    private ClassData getOwnClassData(String className) throws IOException {
        String resource = className.replace('.', '/') + ".class";
        URL url = findResource(resource);
        if (url == null) return null;
        try (InputStream input = url.openStream()) {
            return new ClassData(input.readAllBytes(), codeSource(url, resource));
        }
    }

    private static URL codeSource(URL classResource, String resourcePath) throws IOException {
        if (classResource.openConnection() instanceof JarURLConnection jarConnection) {
            return jarConnection.getJarFileURL();
        }
        if ("file".equals(classResource.getProtocol())) {
            try {
                Path root = Path.of(classResource.toURI());
                for (int i = 0; i < resourcePath.split("/").length; i++) {
                    root = root.getParent();
                }
                return root.toUri().toURL();
            } catch (URISyntaxException e) {
                throw new IOException("Invalid class resource URL: " + classResource, e);
            }
        }
        return classResource;
    }

    private record ClassData(byte[] bytes, URL codeSource) { }

    private boolean isParentFirst(String name) {
        for (String prefix : childFirstPrefixes) if (name.startsWith(prefix)) return false;
        for (String prefix : parentFirstPrefixes) if (name.startsWith(prefix)) return true;
        return false;
    }

    private boolean isChildOnly(String name) {
        for (String prefix : childOnlyPrefixes) if (name.startsWith(prefix)) return true;
        return false;
    }
}
