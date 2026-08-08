package space.nows.mcnows.runtime;

import reactor.util.Logger;
import space.nows.mcnows.api.ClassTransformer;
import space.nows.mcnows.api.ModInitializer;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.api.NowsServices;
import space.nows.mcnows.core.classloading.NowsClassLoader;
import space.nows.mcnows.core.mod.ModContainer;
import space.nows.mcnows.core.mod.ModDiscovery;
import space.nows.mcnows.integration.geb.GebIntegration;
import space.nows.mcnows.integration.kdl.KdlModMetadataReader;
import space.nows.mcnows.integration.logging.NowsLog;
import space.nows.mcnows.minecraft.GameJarLocator;
import space.nows.mcnows.minecraft.LaunchArguments;
import space.nows.mcnows.minecraft.MinecraftCompatibility;
import space.nows.mcnows.mixin.NowsMixinBootstrap;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class NowsLauncher {
    private static final Logger LOG = NowsLog.get(NowsLauncher.class);
    private NowsLauncher() {}

    public static void main(String[] args) throws Exception {
        LOG.info("Nows loader starting");
        try {
            launch(args);
        } catch (Exception | Error failure) {
            LOG.error("Nows loader failed", failure);
            throw failure;
        }
    }

    private static void launch(String[] args) throws Exception {
        LaunchArguments launch = phase("Parse launch arguments", () -> LaunchArguments.parse(args));
        LOG.info("Launch target: Minecraft {}, game directory {}", launch.minecraftVersion(), launch.gameDirectory());

        Path gameJar = phase("Locate Minecraft client JAR", GameJarLocator::locateClientJar);
        LOG.info("Minecraft client JAR: {}", gameJar);

        List<ModContainer> mods = phase("Discover Nows mods", () ->
                ModDiscovery.scan(launch.gameDirectory().resolve("mods"), new KdlModMetadataReader()));
        logDiscoveredMods(mods);

        phase("Validate Minecraft compatibility", () ->
                MinecraftCompatibility.validate(mods, launch.minecraftVersion()));

        List<URL> urls = new ArrayList<>();
        urls.add(gameJar.toUri().toURL());
        for (ModContainer mod : mods) urls.add(mod.path().toUri().toURL());

        try (NowsClassLoader gameLoader = new NowsClassLoader(urls.toArray(URL[]::new), NowsLauncher.class.getClassLoader())) {
            try {
                phase("Configure shared packages", () -> configureSharedPackages(gameLoader));
                Thread.currentThread().setContextClassLoader(gameLoader);

                phase("Install Mixin integration", () -> NowsMixinBootstrap.install(gameLoader, mods));
                phase("Load class transformers", () -> loadTransformers(gameLoader, mods));

                NowsServices services = new NowsServices();
                phase("Install GEB integration", () -> GebIntegration.install(services, gameLoader));

                NowsContext context = new NowsContext(
                        launch.minecraftVersion(), launch.gameDirectory(), mods, gameLoader, services);
                phase("Run mod entrypoints", () -> runEntrypoints(gameLoader, mods, context));

                LOG.info("Starting Minecraft {} with {} Nows mod(s)", launch.minecraftVersion(), mods.size());
                phase("Invoke Minecraft main", () ->
                        invokeMinecraftMain(gameLoader, launch.minecraftArguments().toArray(String[]::new)));
            } finally {
                NowsMixinBootstrap.detach(gameLoader);
                Thread.currentThread().setContextClassLoader(NowsLauncher.class.getClassLoader());
            }
        }
    }

    private static void configureSharedPackages(NowsClassLoader loader) {
        // Parent owns the loader/integration APIs and Minecraft-owned common libraries.
        List<String> prefixes = List.of(
                "space.nows.mcnows.",
                "foo.zaaarf.geb.", "dev.kdl.", "reactor.", "org.reactivestreams.",
                "org.slf4j.", "org.apache.logging.log4j.",
                "org.spongepowered.asm.", "org.objectweb.asm.",
                "com.google.gson.", "com.google.common.", "org.jspecify.",
                "com.lmax.disruptor.");
        for (String prefix : prefixes) {
            loader.addParentFirstPrefix(prefix);
        }
        LOG.debug("Configured {} parent-first package prefix(es)", prefixes.size());
    }

    private static void loadTransformers(NowsClassLoader loader, List<ModContainer> mods) throws Exception {
        for (ModContainer mod : mods) {
            for (String className : mod.descriptor().declarations("transformer")) {
                Object instance = Class.forName(className, true, loader).getDeclaredConstructor().newInstance();
                if (!(instance instanceof ClassTransformer transformer)) {
                    throw new IllegalStateException(className + " does not implement " + ClassTransformer.class.getName());
                }
                loader.addTransformer(transformer);
                LOG.info("Transformer: {} -> {}", mod.descriptor().id(), className);
            }
        }
    }

    private static void runEntrypoints(ClassLoader loader, List<ModContainer> mods, NowsContext context) throws Exception {
        for (ModContainer mod : mods) {
            for (String className : mod.descriptor().declarations("entrypoint")) {
                Object instance = Class.forName(className, true, loader).getDeclaredConstructor().newInstance();
                if (!(instance instanceof ModInitializer initializer)) {
                    throw new IllegalStateException(className + " does not implement " + ModInitializer.class.getName());
                }
                initializer.onInitialize(context);
                LOG.info("Loaded {} {}", mod.descriptor().id(), mod.descriptor().version());
            }
        }
    }

    private static void invokeMinecraftMain(ClassLoader loader, String[] args) throws Exception {
        try {
            Class<?> main = Class.forName("net.minecraft.client.main.Main", true, loader);
            main.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception exception) throw exception;
            if (cause instanceof Error error) throw error;
            throw e;
        }
    }

    private static void logDiscoveredMods(List<ModContainer> mods) {
        if (mods.isEmpty()) {
            LOG.info("No Nows mods discovered");
            return;
        }
        LOG.info("Discovered {} Nows mod(s)", mods.size());
        for (ModContainer mod : mods) {
            LOG.info("Mod: {} {} ({})", mod.descriptor().id(), mod.descriptor().version(), mod.path());
        }
    }

    private static <T> T phase(String name, ThrowingSupplier<T> action) throws Exception {
        NowsLog.Phase phase = NowsLog.phase(LOG, name);
        try {
            T result = action.get();
            phase.close();
            return result;
        } catch (Exception | Error failure) {
            phase.fail(failure);
            throw failure;
        }
    }

    private static void phase(String name, ThrowingRunnable action) throws Exception {
        phase(name, () -> {
            action.run();
            return null;
        });
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
