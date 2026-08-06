package space.nows.mcnows.core.classloading;

import space.nows.mcnows.api.ClassTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
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
    private volatile ClassGenerator classGenerator;

    public NowsClassLoader(URL[] urls, ClassLoader parent) {
        super("nows-game", urls, parent);
        parentFirstPrefixes.addAll(List.of("java.", "javax.", "jakarta.", "jdk.", "sun."));
    }

    public void addParentFirstPrefix(String prefix) { parentFirstPrefixes.add(prefix); }
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
            if (loaded == null) loaded = super.loadClass(name, false);
            if (resolve) resolveClass(loaded);
            return loaded;
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] bytes = getOwnClassBytes(name);
            if (bytes == null && classGenerator != null) bytes = classGenerator.generate(name);
            if (bytes == null) throw new ClassNotFoundException(name);
            for (ClassTransformer transformer : List.copyOf(transformers)) {
                bytes = transformer.transform(name, bytes);
                if (bytes == null) throw new ClassNotFoundException("Transformer returned null for " + name);
            }
            return defineClass(name, bytes, 0, bytes.length);
        } catch (ClassNotFoundException e) {
            throw e;
        } catch (Throwable t) {
            throw new ClassNotFoundException("Failed to define " + name, t);
        }
    }

    private byte[] getOwnClassBytes(String className) throws IOException {
        String resource = className.replace('.', '/') + ".class";
        URL url = findResource(resource);
        if (url == null) return null;
        try (InputStream input = url.openStream()) { return input.readAllBytes(); }
    }

    private boolean isParentFirst(String name) {
        for (String prefix : parentFirstPrefixes) if (name.startsWith(prefix)) return true;
        return false;
    }
}
