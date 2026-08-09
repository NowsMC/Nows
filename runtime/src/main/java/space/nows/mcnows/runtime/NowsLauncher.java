package space.nows.mcnows.runtime;

import reactor.util.Logger;
import space.nows.mcnows.api.ClassTransformer;
import space.nows.mcnows.api.ModInitializer;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.api.NowsSide;
import space.nows.mcnows.api.NowsServices;
import space.nows.mcnows.core.classloading.NowsClassLoader;
import space.nows.mcnows.core.mod.ModContainer;
import space.nows.mcnows.core.mod.ModDependencyResolver;
import space.nows.mcnows.core.mod.ModDiscovery;
import space.nows.mcnows.integration.geb.GebIntegration;
import space.nows.mcnows.integration.geb.NowsEvents;
import space.nows.mcnows.integration.geb.event.NowsBootstrapReadyEvent;
import space.nows.mcnows.integration.geb.event.NowsEntrypointsCompletedEvent;
import space.nows.mcnows.integration.geb.event.NowsEntrypointsStartingEvent;
import space.nows.mcnows.integration.geb.event.NowsMinecraftStartingEvent;
import space.nows.mcnows.integration.geb.event.NowsModEntrypointCompletedEvent;
import space.nows.mcnows.integration.geb.event.NowsModEntrypointStartingEvent;
import space.nows.mcnows.integration.kdl.KdlModMetadataReader;
import space.nows.mcnows.integration.logging.NowsLog;
import space.nows.mcnows.integration.network.NowsNetworking;
import space.nows.mcnows.minecraft.GameJarLocator;
import space.nows.mcnows.minecraft.LaunchArguments;
import space.nows.mcnows.minecraft.MinecraftCompatibility;
import space.nows.mcnows.minecraft.MinecraftVersionPolicy;
import space.nows.mcnows.mixin.NowsMixinBootstrap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class NowsLauncher {
    private static final Logger LOG = NowsLog.get(NowsLauncher.class);
    private static final NowsSide RUNTIME_SIDE = NowsSide.CLIENT;
    private NowsLauncher() {}

    public static void main(String[] args) throws Exception {
        LOG.info("Nows loader starting");
        LOG.info("Nows runtime version: {}", nowsVersion());
        LOG.info("Java runtime: {} {} ({})",
                System.getProperty("java.vendor"),
                System.getProperty("java.version"),
                System.getProperty("java.vm.name"));
        LOG.info("Process working directory: {}", Path.of("").toAbsolutePath().normalize());
        try {
            launch(args);
        } catch (Exception | Error failure) {
            LOG.error("Nows loader failed", failure);
            throw failure;
        }
    }

    private static void launch(String[] args) throws Exception {
        LaunchArguments launch = phase("Parse launch arguments", () -> LaunchArguments.parse(args));
        LOG.info("Launch target: Minecraft {}, side {}, game directory {}",
                launch.minecraftVersion(), RUNTIME_SIDE.metadataName(), launch.gameDirectory());
        LOG.info("Launcher profile id: {}", launch.profileId() == null ? "<unknown>" : launch.profileId());
        LOG.info("Minecraft root directory: {}", launch.minecraftDirectory());
        LOG.info("Minecraft argument summary: {} forwarded argument(s), access token hidden",
                launch.minecraftArguments().size());

        MinecraftVersionPolicy policy = phase("Load Minecraft version policy", () ->
                MinecraftVersionPolicy.load(launch.minecraftVersion()));
        LOG.info("Minecraft policy: {} main class {} ({})",
                policy.minecraftVersion(), policy.clientMainClass(),
                policy.bundled() ? policy.resourcePath() : "default policy");
        LOG.info("Built-in Mixin configs from policy: {}",
                policy.builtInMixinConfigs().isEmpty() ? "<none>" : String.join(", ", policy.builtInMixinConfigs()));

        Path gameJar = phase("Locate Minecraft client JAR", GameJarLocator::locateClientJar);
        LOG.info("Minecraft client JAR: {}", gameJar);

        Path gameModsDirectory = launch.gameDirectory().resolve("mods");
        Path profileModsDirectory = optionalProfileModsDirectory(launch);
        List<Path> modsDirectories = List.of(gameModsDirectory, profileModsDirectory);
        phase("Inspect Nows mods directories", () -> logModDirectories(gameModsDirectory, profileModsDirectory));
        List<ModContainer> discoveredMods = phase("Discover Nows mods", () ->
                ModDiscovery.scan(modsDirectories, new KdlModMetadataReader()));
        List<ModContainer> mods = phase("Resolve mod dependencies", () ->
                ModDependencyResolver.resolve(discoveredMods, Map.of(
                        "minecraft", launch.minecraftVersion(),
                        "nows", nowsVersion(),
                        "nows-loader", nowsVersion())));
        logDiscoveredMods(mods);

        phase("Validate Minecraft compatibility", () ->
                MinecraftCompatibility.validate(mods, launch.minecraftVersion(), RUNTIME_SIDE));

        List<URL> urls = new ArrayList<>();
        urls.add(gameJar.toUri().toURL());
        for (ModContainer mod : mods) urls.add(mod.path().toUri().toURL());
        LOG.info("Game classloader URLs: {} total (1 Minecraft client + {} mod jar(s))", urls.size(), mods.size());

        try (NowsClassLoader gameLoader = new NowsClassLoader(urls.toArray(URL[]::new), NowsLauncher.class.getClassLoader())) {
            try {
                LOG.info("Game classloader: {} with parent {}", gameLoader.getName(), gameLoader.getParent());
                phase("Configure shared packages", () -> configureSharedPackages(gameLoader));
                Thread.currentThread().setContextClassLoader(gameLoader);
                LOG.info("Thread context classloader switched to {}", gameLoader.getName());

                phase("Configure Minecraft client hooks", () ->
                        configureMinecraftClientHooks(launch.minecraftVersion(), mods.size()));
                phase("Install Mixin integration", () ->
                        NowsMixinBootstrap.install(gameLoader, policy.builtInMixinConfigs(), mods));
                phase("Load class transformers", () -> loadTransformers(gameLoader, mods));

                NowsServices services = new NowsServices();
                phase("Install Minecraft API adapter", () ->
                        installMinecraftApiAdapter(services, launch.gameDirectory(), launch.minecraftVersion()));
                phase("Install network integration", () -> NowsNetworking.install(services, RUNTIME_SIDE));
                LOG.info("Service registered: {} -> {}", "NowsNetworking",
                        services.require(NowsNetworking.class).getClass().getName());
                phase("Install GEB integration", () -> GebIntegration.install(services, gameLoader));
                LOG.info("Service registered: {} -> {}", "GEB", services.require(foo.zaaarf.geb.GEB.class).getClass().getName());

                NowsContext context = new NowsContext(
                        launch.minecraftVersion(), RUNTIME_SIDE, launch.gameDirectory(), mods, gameLoader, services);
                NowsNetworking networking = NowsNetworking.service(context);
                int networkChannelCount = phase("Register network channels", () ->
                        networking.registerDeclaredChannels(mods));
                LOG.info("Registered {} declared network channel(s)", networkChannelCount);
                NowsEvents events = GebIntegration.events(context);
                int listenerCount = phase("Register GEB listeners", () ->
                        GebIntegration.registerDeclaredListeners(context));
                LOG.info("Registered {} declared GEB listener(s)", listenerCount);
                events.post(new NowsBootstrapReadyEvent(context));

                phase("Run mod entrypoints", () -> runEntrypoints(gameLoader, mods, context, events));

                LOG.info("Starting Minecraft {} with {} Nows mod(s)", launch.minecraftVersion(), mods.size());
                events.post(new NowsMinecraftStartingEvent(
                        context, policy.clientMainClass(), launch.minecraftArguments().size()));
                phase("Invoke Minecraft main", () ->
                        invokeMinecraftMain(gameLoader, policy.clientMainClass(),
                                launch.minecraftArguments().toArray(String[]::new)));
            } finally {
                LOG.info("Detaching Nows integrations and restoring launcher classloader");
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
                "io.netty.",
                "org.slf4j.", "org.apache.logging.log4j.",
                "org.spongepowered.asm.", "org.objectweb.asm.",
                "com.google.gson.", "com.google.common.", "org.jspecify.",
                "com.lmax.disruptor.");
        for (String prefix : prefixes) {
            loader.addParentFirstPrefix(prefix);
        }
        LOG.info("Parent-first package prefixes ({}): {}", prefixes.size(), String.join(", ", prefixes));
    }

    private static void configureMinecraftClientHooks(String minecraftVersion, int modCount) throws Exception {
        String hookClassName = "space.nows.mcnows.mc.internal.NowsMinecraftClientHooks";
        try {
            Class<?> hookClass = Class.forName(hookClassName, true, NowsLauncher.class.getClassLoader());
            Method configure = hookClass.getMethod("configure", String.class, String.class, int.class);
            configure.invoke(null, nowsVersion(), minecraftVersion, modCount);
            LOG.info("Minecraft client hooks configured from {} for {} mod(s)", hookClassName, modCount);
        } catch (ClassNotFoundException ignored) {
            LOG.info("No Minecraft client hooks available for {}", minecraftVersion);
        }
    }

    private static void installMinecraftApiAdapter(
            NowsServices services,
            Path gameDirectory,
            String minecraftVersion
    ) throws Exception {
        String integrationClassName = "space.nows.mcnows.mc.internal.NowsMinecraftIntegration";
        try {
            Class<?> integrationClass = Class.forName(integrationClassName, true, NowsLauncher.class.getClassLoader());
            Method install = integrationClass.getMethod("install", NowsServices.class, Path.class);
            install.invoke(null, services, gameDirectory);
            LOG.info("Minecraft API adapter installed from {}", integrationClassName);
        } catch (ClassNotFoundException ignored) {
            LOG.info("No Minecraft API adapter available for {}", minecraftVersion);
        }
    }

    private static String nowsVersion() {
        Package runtimePackage = NowsLauncher.class.getPackage();
        String implementationVersion = runtimePackage == null ? null : runtimePackage.getImplementationVersion();
        if (implementationVersion != null && !implementationVersion.isBlank()) {
            return implementationVersion;
        }
        String systemVersion = System.getProperty("nows.version");
        if (systemVersion != null && !systemVersion.isBlank()) {
            return systemVersion;
        }
        Properties version = new Properties();
        try (InputStream input = NowsLauncher.class.getResourceAsStream("/META-INF/nows/runtime/version.properties")) {
            if (input != null) {
                version.load(input);
            }
        } catch (IOException ignored) {
            return "development";
        }
        return version.getProperty("nows.version", "development").trim();
    }

    private static void loadTransformers(NowsClassLoader loader, List<ModContainer> mods) throws Exception {
        int count = 0;
        for (ModContainer mod : mods) {
            for (String className : mod.descriptor().declarations("transformer")) {
                Object instance = Class.forName(className, true, loader).getDeclaredConstructor().newInstance();
                if (!(instance instanceof ClassTransformer transformer)) {
                    throw new IllegalStateException(className + " does not implement " + ClassTransformer.class.getName());
                }
                loader.addTransformer(transformer);
                count++;
                LOG.info("Transformer: {} -> {}", mod.descriptor().id(), className);
            }
        }
        if (count == 0) {
            LOG.info("No mod class transformers declared");
        } else {
            LOG.info("Loaded {} mod class transformer(s)", count);
        }
    }

    private static void runEntrypoints(
            ClassLoader loader,
            List<ModContainer> mods,
            NowsContext context,
            NowsEvents events
    ) throws Exception {
        int count = 0;
        events.post(new NowsEntrypointsStartingEvent(context));
        for (ModContainer mod : mods) {
            for (String className : mod.descriptor().declarations("entrypoint")) {
                LOG.info("Running entrypoint: {} -> {}", mod.descriptor().id(), className);
                events.post(new NowsModEntrypointStartingEvent(context, mod, className));
                Object instance = Class.forName(className, true, loader).getDeclaredConstructor().newInstance();
                if (!(instance instanceof ModInitializer initializer)) {
                    throw new IllegalStateException(className + " does not implement " + ModInitializer.class.getName());
                }
                initializer.onInitialize(context);
                count++;
                events.post(new NowsModEntrypointCompletedEvent(context, mod, className));
                LOG.info("Loaded {} {}", mod.descriptor().id(), mod.descriptor().version());
            }
        }
        if (count == 0) {
            LOG.info("No mod entrypoints declared");
        } else {
            LOG.info("Ran {} mod entrypoint(s)", count);
        }
        events.post(new NowsEntrypointsCompletedEvent(context, count));
    }

    private static void invokeMinecraftMain(ClassLoader loader, String mainClassName, String[] args) throws Exception {
        try {
            LOG.info("Invoking Minecraft main class {} with {} argument(s)", mainClassName, args.length);
            Class<?> main = Class.forName(mainClassName, true, loader);
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

    private static Path optionalProfileModsDirectory(LaunchArguments launch) {
        String profileId = launch.profileId();
        if (profileId == null || profileId.isBlank()) {
            profileId = "nows-" + nowsVersion() + "-" + launch.minecraftVersion();
        }
        return launch.minecraftDirectory()
                .resolve("nows")
                .resolve("profiles")
                .resolve(profileId)
                .resolve("mods");
    }

    private static void logModDirectories(Path gameModsDirectory, Path profileModsDirectory) throws IOException {
        Path main = gameModsDirectory.toAbsolutePath().normalize();
        Path profile = profileModsDirectory.toAbsolutePath().normalize();
        logModDirectory("Game mods directory", main);
        if (profile.equals(main)) {
            LOG.info("Optional Nows profile mods directory is the same as the game mods directory");
        } else {
            logModDirectory("Optional Nows profile mods directory", profile);
        }
    }

    private static void logModDirectory(String label, Path modsDirectory) throws IOException {
        Files.createDirectories(modsDirectory);
        LOG.info("{}: {}", label, modsDirectory);
        try (var paths = Files.list(modsDirectory)) {
            List<Path> jars = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
            if (jars.isEmpty()) {
                LOG.info("{} contains no jar candidates", label);
                return;
            }
            LOG.info("{} jar candidates: {}", label, jars.size());
            for (Path jar : jars) {
                LOG.info("{} candidate: {}", label, jar.toAbsolutePath().normalize());
            }
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
