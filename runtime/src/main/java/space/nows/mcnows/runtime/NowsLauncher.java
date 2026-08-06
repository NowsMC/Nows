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
        LaunchArguments launch = LaunchArguments.parse(args);
        Path gameJar = GameJarLocator.locateClientJar();
        List<ModContainer> mods = ModDiscovery.scan(launch.gameDirectory().resolve("mods"), new KdlModMetadataReader());
        MinecraftCompatibility.validate(mods, launch.minecraftVersion());

        List<URL> urls = new ArrayList<>();
        urls.add(gameJar.toUri().toURL());
        for (ModContainer mod : mods) urls.add(mod.path().toUri().toURL());

        try (NowsClassLoader gameLoader = new NowsClassLoader(urls.toArray(URL[]::new), NowsLauncher.class.getClassLoader())) {
            configureSharedPackages(gameLoader);
            Thread.currentThread().setContextClassLoader(gameLoader);

            NowsMixinBootstrap.install(gameLoader, mods);
            loadTransformers(gameLoader, mods);

            NowsServices services = new NowsServices();
            GebIntegration.install(services, gameLoader);

            NowsContext context = new NowsContext(
                    launch.minecraftVersion(), launch.gameDirectory(), mods, gameLoader, services);
            runEntrypoints(gameLoader, mods, context);

            LOG.info("Starting Minecraft {} with {} Nows mod(s)", launch.minecraftVersion(), mods.size());
            invokeMinecraftMain(gameLoader, launch.minecraftArguments().toArray(String[]::new));
        }
    }

    private static void configureSharedPackages(NowsClassLoader loader) {
        // Parent owns the loader/integration APIs and Minecraft-owned common libraries.
        for (String prefix : List.of(
                "space.nows.mcnows.",
                "foo.zaaarf.geb.", "dev.kdl.", "reactor.", "org.reactivestreams.",
                "org.slf4j.", "org.apache.logging.log4j.",
                "org.spongepowered.asm.", "org.objectweb.asm.",
                "com.google.gson.", "com.google.common.", "org.jspecify.",
                "com.lmax.disruptor.")) {
            loader.addParentFirstPrefix(prefix);
        }
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
}
