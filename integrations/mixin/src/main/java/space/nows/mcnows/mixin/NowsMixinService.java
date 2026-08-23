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

package space.nows.mcnows.mixin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;
import org.spongepowered.asm.service.IClassTracker;
import org.spongepowered.asm.service.IMixinAuditTrail;
import org.spongepowered.asm.service.IMixinInternal;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.ITransformer;
import org.spongepowered.asm.service.ITransformerProvider;
import org.spongepowered.asm.util.ReEntranceLock;
import reactor.util.Logger;
import space.nows.mcnows.core.classloading.NowsClassLoader;
import space.nows.mcnows.integration.logging.NowsLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Connects SpongePowered Mixin directly to NowsClassLoader; no javaagent is involved. */
public final class NowsMixinService implements IMixinService, IClassProvider, IClassBytecodeProvider,
        ITransformerProvider, IClassTracker {

    private static final Logger LOG = NowsLog.get(NowsMixinService.class);
    private static final IMixinAuditTrail AUDIT_TRAIL = new NowsMixinAuditTrail();
    private static final Set<String> INVALID_CLASSES = ConcurrentHashMap.newKeySet();
    private static final Set<String> TRANSFORMER_EXCLUSIONS = ConcurrentHashMap.newKeySet();
    private static volatile NowsClassLoader loader;
    private static volatile IMixinTransformer transformer;
    private final ReEntranceLock lock = new ReEntranceLock(1);

    public static void attach(NowsClassLoader targetLoader) {
        if (targetLoader == null) {
            throw new IllegalArgumentException("Nows Mixin service cannot attach a null game class loader");
        }
        NowsClassLoader previous = loader;
        if (previous != null && previous != targetLoader) {
            LOG.warn("Replacing attached Nows Mixin class loader; previous loader should already be detached");
        }
        loader = targetLoader;
        INVALID_CLASSES.clear();
        TRANSFORMER_EXCLUSIONS.clear();
        LOG.info("Nows Mixin service attached to {} with {} URL(s)", targetLoader.getName(), targetLoader.getURLs().length);
    }

    public static void detach(NowsClassLoader targetLoader) {
        NowsClassLoader current = loader;
        if (current == null) {
            return;
        }
        if (current != targetLoader) {
            LOG.warn("Ignoring request to detach non-current Nows Mixin class loader {}", targetLoader);
            return;
        }
        loader = null;
        INVALID_CLASSES.clear();
        TRANSFORMER_EXCLUSIONS.clear();
        LOG.info("Nows Mixin service detached from {}", targetLoader.getName());
    }

    public static IMixinTransformer transformer() {
        return transformer;
    }

    private static NowsClassLoader loader() {
        NowsClassLoader value = loader;
        if (value == null) throw new IllegalStateException("Nows Mixin service was used before the game class loader was attached");
        return value;
    }

    private byte[] getClassBytes(String name, String transformedName) throws IOException {
        byte[] bytes = loader().getRawClassBytes(name);
        if (bytes == null) throw new IOException("Class not found: " + name);
        return bytes;
    }

    private byte[] getClassBytes(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        if (runTransformers) {
            LOG.trace("Mixin requested transformed bytecode for {}; Nows provides raw bytes before its transformer chain", name);
        }
        byte[] bytes = loader().getRawClassBytes(name);
        if (bytes == null) throw new ClassNotFoundException(name);
        return bytes;
    }

    @Override
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        return getClassNode(name, true, 0);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        return getClassNode(name, runTransformers, 0);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers, int readerFlags) throws ClassNotFoundException, IOException {
        ClassReader reader = new ClassReader(getClassBytes(name, runTransformers));
        ClassNode node = new ClassNode();
        reader.accept(node, readerFlags);
        return node;
    }

    @Override
    public URL[] getClassPath() {
        return loader().getURLs();
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return loader().loadClass(name);
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, loader());
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, NowsMixinService.class.getClassLoader());
    }

    @Override
    public String getName() {
        return "Nows";
    }

    @Override
    public boolean isValid() {
        return loader != null;
    }

    @Override
    public void prepare() {
        LOG.info("Nows Mixin service prepare");
    }

    @Override
    public MixinEnvironment.Phase getInitialPhase() {
        return MixinEnvironment.Phase.PREINIT;
    }

    @Override
    public void offer(IMixinInternal internal) {
        if (internal instanceof IMixinTransformerFactory factory) {
            transformer = factory.createTransformer();
            LOG.info("Mixin transformer factory offered {}", internal.getClass().getName());
        }
    }

    @Override
    public void init() {
        LOG.info("Nows Mixin service init");
    }

    @Override
    public void beginPhase() {
        LOG.info("Nows Mixin service begin phase");
    }

    @Override
    public void checkEnv(Object bootSource) {
    }

    @Override
    public ReEntranceLock getReEntranceLock() {
        return lock;
    }

    @Override
    public IClassProvider getClassProvider() {
        return this;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return this;
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return this;
    }

    @Override
    public IClassTracker getClassTracker() {
        return this;
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return AUDIT_TRAIL;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return Collections.singletonList("org.spongepowered.asm.launch.platform.MixinPlatformAgentDefault");
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        URL source = NowsMixinService.class.getProtectionDomain().getCodeSource().getLocation();
        return new ContainerHandleURI(URI.create(source.toString()));
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return Collections.emptyList();
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return loader().getResourceAsStream(name);
    }

    @Override
    public void registerInvalidClass(String className) {
        if (className == null || className.isBlank()) {
            return;
        }
        INVALID_CLASSES.add(className);
        LOG.debug("Mixin registered invalid class {}", className);
    }

    @Override
    public boolean isClassLoaded(String className) {
        return loader().isClassLoaded(className);
    }

    @Override
    public String getClassRestrictions(String className) {
        if (className == null || className.isBlank()) {
            return "";
        }
        if (INVALID_CLASSES.contains(className)) {
            return "INVALID";
        }
        for (String exclusion : TRANSFORMER_EXCLUSIONS) {
            if (className.startsWith(exclusion)) {
                return "PACKAGE_CLASSLOADER_EXCLUSION";
            }
        }
        return "";
    }

    @Override
    public Collection<ITransformer> getTransformers() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ITransformer> getDelegatedTransformers() {
        return Collections.emptyList();
    }

    @Override
    public void addTransformerExclusion(String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        TRANSFORMER_EXCLUSIONS.add(name);
        LOG.debug("Mixin transformer exclusion {}", name);
    }

    @Override
    public String getSideName() {
        return "CLIENT";
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_8;
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_21;
    }

    @Override
    public ILogger getLogger(String name) {
        return NowsMixinLogger.get(name);
    }

    private static final class NowsMixinAuditTrail implements IMixinAuditTrail {
        @Override
        public void onApply(String targetClassName, String mixinClassName) {
            if ("net.minecraft.client.gui.screens.TitleScreen".equals(targetClassName)) {
                LOG.info("Mixin applied: {} -> {}", mixinClassName, targetClassName);
            } else {
                LOG.debug("Mixin applied: {} -> {}", mixinClassName, targetClassName);
            }
        }

        @Override
        public void onPostProcess(String className) {
            LOG.trace("Mixin post-processed {}", className);
        }

        @Override
        public void onGenerate(String generatorName, String className) {
            LOG.debug("Mixin generated: {} by {}", className, generatorName);
        }
    }
}
