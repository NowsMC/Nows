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

package space.nows.loader.runtime;

import reactor.util.Logger;
import space.nows.platform.api.ClassTransformer;
import space.nows.platform.api.ModInitializer;
import space.nows.platform.api.NowsContext;
import space.nows.platform.api.NowsSide;
import space.nows.platform.api.NowsServices;
import space.nows.platform.api.config.NowsConfigFiles;
import space.nows.platform.core.classloading.NowsClassLoader;
import space.nows.platform.core.mod.ModContainer;
import space.nows.platform.core.mod.ModDependencyResolver;
import space.nows.platform.core.mod.ModDiscovery;
import space.nows.integration.geb.GebIntegration;
import space.nows.integration.geb.NowsEvents;
import space.nows.integration.geb.event.NowsBootstrapReadyEvent;
import space.nows.integration.geb.event.NowsEntrypointsCompletedEvent;
import space.nows.integration.geb.event.NowsEntrypointsStartingEvent;
import space.nows.integration.geb.event.NowsMinecraftStartingEvent;
import space.nows.integration.geb.event.NowsModEntrypointCompletedEvent;
import space.nows.integration.geb.event.NowsModEntrypointStartingEvent;
import space.nows.integration.geb.event.NowsRegisterEvent;
import space.nows.integration.kdl.KdlModMetadataReader;
import space.nows.integration.logging.NowsLog;
import space.nows.integration.network.NowsNetworking;
import space.nows.minecraft.GameJarLocator;
import space.nows.minecraft.LaunchArguments;
import space.nows.minecraft.MinecraftCompatibility;
import space.nows.minecraft.MinecraftVersionPolicy;
import space.nows.integration.mixin.NowsMixinBootstrap;
import space.nows.platform.core.loading.NowsLoadingState;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public final class NowsLauncher {
    private static final Logger LOG = NowsLog.get(NowsLauncher.class);
    private static final int BOOTSTRAP_PHASES = 18;
    private static final String MINECRAFT_ADAPTER_PATH_PROPERTY = "nows.minecraftAdapterPath";
    private static final String MINECRAFT_ADAPTER_MARKER =
            "space/nows/mc/internal/MinecraftIntegration.class";
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
        NowsLoadingState.start("Nows Loader", BOOTSTRAP_PHASES);
        LaunchArguments launch = phase("Parse launch arguments", () -> LaunchArguments.parse(args));
        NowsSide runtimeSide = launch.side();
        LOG.info("Launch target: Minecraft {}, side {}, game directory {}",
                launch.minecraftVersion(), runtimeSide.metadataName(), launch.gameDirectory());
        LOG.info("Launcher profile id: {}", launch.profileId() == null ? "<unknown>" : launch.profileId());
        LOG.info("Minecraft root directory: {}", launch.minecraftDirectory());
        LOG.info("Minecraft argument summary: {} forwarded argument(s), access token hidden",
                launch.minecraftArguments().size());

        MinecraftVersionPolicy policy = phase("Load Minecraft version policy", () ->
                MinecraftVersionPolicy.load(launch.minecraftVersion()));
        String minecraftMainClass = policy.mainClass(runtimeSide);
        List<String> builtInMixinConfigs = policy.builtInMixinConfigs(runtimeSide);
        LOG.info("Minecraft policy: {} {} main class {} ({})",
                policy.minecraftVersion(), runtimeSide.metadataName(), minecraftMainClass,
                policy.bundled() ? policy.resourcePath() : "default policy");
        LOG.info("Built-in Mixin configs from policy: {}",
                builtInMixinConfigs.isEmpty() ? "<none>" : String.join(", ", builtInMixinConfigs));

        Path gameJar = phase("Locate Minecraft " + runtimeSide.metadataName() + " JAR", () ->
                GameJarLocator.locate(runtimeSide));
        LOG.info("Minecraft {} JAR: {}", runtimeSide.metadataName(), gameJar);

        Path gameModsDirectory = launch.gameDirectory().resolve("mods");
        Path profileModsDirectory = optionalProfileModsDirectory(launch);
        List<Path> modsDirectories = List.of(gameModsDirectory, profileModsDirectory);
        phase("Inspect Nows mods directories", () -> logModDirectories(gameModsDirectory, profileModsDirectory));
        List<ModContainer> discoveredMods = phase("Discover Nows mods", () ->
                ModDiscovery.scan(modsDirectories, new KdlModMetadataReader()));
        phase("Validate Minecraft compatibility", () ->
                MinecraftCompatibility.validate(discoveredMods, launch.minecraftVersion(), runtimeSide));
        List<ModContainer> mods = phase("Resolve mod dependencies", () ->
                ModDependencyResolver.resolve(discoveredMods, Map.of(
                        "minecraft", launch.minecraftVersion(),
                        "nows", nowsVersion(),
                        "nows-loader", nowsVersion())));
        logDiscoveredMods(mods);

        List<URL> urls = new ArrayList<>();
        urls.add(gameJar.toUri().toURL());
        Optional<URL> adapterUrl = runtimeSide == NowsSide.CLIENT ? minecraftAdapterUrl(policy) : Optional.empty();
        adapterUrl.ifPresent(urls::add);
        for (ModContainer mod : mods) urls.add(mod.path().toUri().toURL());
        adapterUrl.ifPresentOrElse(
                url -> LOG.info("Minecraft API adapter classpath: {}", url),
                () -> LOG.info("No Minecraft API adapter classpath found for {}", launch.minecraftVersion()));
        LOG.info("Game classloader URLs: {} total (1 Minecraft {} + {} adapter + {} mod jar(s))",
                urls.size(), runtimeSide.metadataName(), adapterUrl.isPresent() ? 1 : 0, mods.size());

        try (NowsClassLoader gameLoader = new NowsClassLoader(urls.toArray(URL[]::new), NowsLauncher.class.getClassLoader())) {
            try {
                LOG.info("Game classloader: {} with parent {}", gameLoader.getName(), gameLoader.getParent());
                phase("Configure shared packages", () -> configureSharedPackages(gameLoader));
                Thread.currentThread().setContextClassLoader(gameLoader);
                LOG.info("Thread context classloader switched to {}", gameLoader.getName());

                if (runtimeSide == NowsSide.CLIENT) {
                    phase("Configure Minecraft client hooks", () ->
                            configureMinecraftClientHooks(gameLoader, launch.minecraftVersion(), mods));
                }
                phase("Install Mixin integration", () ->
                        NowsMixinBootstrap.install(gameLoader, builtInMixinConfigs, mods));
                phase("Load class transformers", () -> loadTransformers(gameLoader, mods));

                NowsServices services = new NowsServices();
                services.register(NowsConfigFiles.class,
                        new NowsConfigFiles(launch.gameDirectory().resolve("config").resolve("nows")));
                phase("Install Minecraft API adapter", () ->
                        installMinecraftApiAdapter(gameLoader, services, launch.gameDirectory(), launch.minecraftVersion(), runtimeSide));
                phase("Install network integration", () -> NowsNetworking.install(services, runtimeSide));
                LOG.info("Service registered: {} -> {}", "NowsNetworking",
                        services.require(NowsNetworking.class).getClass().getName());
                phase("Install GEB integration", () -> GebIntegration.install(services, gameLoader));
                LOG.info("Service registered: {} -> {}", "GEB", services.require(foo.zaaarf.geb.GEB.class).getClass().getName());

                NowsContext context = new NowsContext(
                        launch.minecraftVersion(), runtimeSide, launch.gameDirectory(), mods, gameLoader, services);
                if (runtimeSide == NowsSide.CLIENT) {
                    phase("Install built-in Minecraft UI", () ->
                            installBuiltInMinecraftUi(gameLoader, context, launch.minecraftVersion()));
                }
                NowsNetworking networking = NowsNetworking.service(context);
                int networkChannelCount = phase("Register network channels", () ->
                        networking.registerDeclaredChannels(mods));
                LOG.info("Registered {} declared network channel(s)", networkChannelCount);
                NowsEvents events = GebIntegration.events(context);
                int listenerCount = phase("Register GEB listeners", () ->
                        GebIntegration.registerDeclaredListeners(context));
                LOG.info("Registered {} declared GEB listener(s)", listenerCount);
                events.post(new NowsBootstrapReadyEvent(context));
                events.post(new NowsRegisterEvent(context));

                phase("Run mod entrypoints", () -> runEntrypoints(gameLoader, mods, context, events));

                LOG.info("Starting Minecraft {} with {} Nows mod(s)", launch.minecraftVersion(), mods.size());
                events.post(new NowsMinecraftStartingEvent(
                        context, minecraftMainClass, launch.minecraftArguments().size()));
                phase("Invoke Minecraft main", () ->
                        invokeMinecraftMain(gameLoader, minecraftMainClass,
                                launch.minecraftArguments().toArray(String[]::new)));
            } finally {
                LOG.info("Detaching Nows integrations and restoring launcher classloader");
                NowsMixinBootstrap.detach(gameLoader);
                Thread.currentThread().setContextClassLoader(NowsLauncher.class.getClassLoader());
            }
        }
    }

    private static void configureSharedPackages(NowsClassLoader loader) {
        // Parent owns stable loader/integration APIs and Minecraft-owned common libraries.
        // Version-specific Minecraft adapters stay child-first with the game classes they reference.
        List<String> childFirstPrefixes = List.of("space.nows.mc.");
        List<String> childOnlyPrefixes = List.of("net.minecraft.");
        List<String> prefixes = List.of(
                "space.nows.platform.api.",
                "space.nows.platform.core.",
                "space.nows.platform.core.loading.",
                "space.nows.integration.",
                "space.nows.minecraft.",
                "space.nows.integration.mixin.",
                "foo.zaaarf.geb.", "dev.kdl.", "reactor.", "org.reactivestreams.",
                "io.netty.",
                "com.squareup.moshi.", "okio.", "kotlin.", "org.jetbrains.",
                "org.slf4j.", "org.apache.logging.log4j.",
                "org.spongepowered.asm.", "org.objectweb.asm.",
                "com.google.gson.", "com.google.common.", "org.jspecify.",
                "com.lmax.disruptor.");
        for (String prefix : childFirstPrefixes) {
            loader.addChildFirstPrefix(prefix);
        }
        for (String prefix : childOnlyPrefixes) {
            loader.addChildOnlyPrefix(prefix);
        }
        for (String prefix : prefixes) {
            loader.addParentFirstPrefix(prefix);
        }
        LOG.info("Child-first package prefixes ({}): {}", childFirstPrefixes.size(), String.join(", ", childFirstPrefixes));
        LOG.info("Child-only package prefixes ({}): {}", childOnlyPrefixes.size(), String.join(", ", childOnlyPrefixes));
        LOG.info("Parent-first package prefixes ({}): {}", prefixes.size(), String.join(", ", prefixes));
    }

    private static void configureMinecraftClientHooks(
            ClassLoader loader,
            String minecraftVersion,
            List<ModContainer> mods
    ) throws Exception {
        String hookClassName = "space.nows.mc.internal.ClientHooks";
        try {
            Class<?> hookClass = Class.forName(hookClassName, true, loader);
            try {
                Method configure = hookClass.getMethod("configure", String.class, String.class, int.class, List.class);
                configure.invoke(null, nowsVersion(), minecraftVersion, mods.size(), List.copyOf(mods));
            } catch (NoSuchMethodException ignored) {
                Method configure = hookClass.getMethod("configure", String.class, String.class, int.class);
                configure.invoke(null, nowsVersion(), minecraftVersion, mods.size());
            }
            LOG.info("Minecraft client hooks configured from {} for {} mod(s)", hookClassName, mods.size());
        } catch (ClassNotFoundException ignored) {
            LOG.info("No Minecraft client hooks available for {}", minecraftVersion);
        }
    }

    private static void installMinecraftApiAdapter(
            ClassLoader loader,
            NowsServices services,
            Path gameDirectory,
            String minecraftVersion,
            NowsSide runtimeSide
    ) throws Exception {
        if (runtimeSide == NowsSide.SERVER) {
            LOG.info("Skipping Minecraft API adapter on server runtime; current adapters are client-classpath scoped");
            return;
        }
        String integrationClassName = "space.nows.mc.internal.MinecraftIntegration";
        try {
            Class<?> integrationClass = Class.forName(integrationClassName, true, loader);
            Method install = integrationClass.getMethod("install", NowsServices.class, Path.class);
            install.invoke(null, services, gameDirectory);
            LOG.info("Minecraft API adapter installed from {}", integrationClassName);
        } catch (ClassNotFoundException ignored) {
            LOG.info("No Minecraft API adapter available for {}", minecraftVersion);
        }
    }

    private static void installBuiltInMinecraftUi(
            ClassLoader loader,
            NowsContext context,
            String minecraftVersion
    ) throws Exception {
        String integrationClassName = "space.nows.mc.internal.MinecraftIntegration";
        try {
            Class<?> integrationClass = Class.forName(integrationClassName, true, loader);
            try {
                Method installBuiltInUi = integrationClass.getMethod("installBuiltInUi", NowsContext.class);
                installBuiltInUi.invoke(null, context);
                LOG.info("Built-in Minecraft UI installed from {}", integrationClassName);
            } catch (NoSuchMethodException ignored) {
                LOG.info("Minecraft API adapter for {} has no built-in UI hook", minecraftVersion);
            }
        } catch (ClassNotFoundException ignored) {
            LOG.info("No Minecraft API adapter available for built-in UI on {}", minecraftVersion);
        }
    }

    private static Optional<URL> minecraftAdapterUrl(MinecraftVersionPolicy policy) throws IOException {
        if (!policy.bundled()) {
            return Optional.empty();
        }
        String configuredPath = System.getProperty(MINECRAFT_ADAPTER_PATH_PROPERTY, "").trim();
        if (!configuredPath.isEmpty()) {
            Path path = Path.of(configuredPath).toAbsolutePath().normalize();
            if (!Files.isRegularFile(path)) {
                throw new IOException("Configured Minecraft adapter JAR is missing: " + path);
            }
            return Optional.of(path.toUri().toURL());
        }
        URL resource = NowsLauncher.class.getClassLoader().getResource(MINECRAFT_ADAPTER_MARKER);
        if (resource == null) {
            return Optional.empty();
        }
        return resourceContainerUrl(resource, MINECRAFT_ADAPTER_MARKER);
    }

    private static Optional<URL> resourceContainerUrl(URL resource, String resourcePath) throws IOException {
        URLConnection connection = resource.openConnection();
        if (connection instanceof JarURLConnection jarConnection) {
            return Optional.of(jarConnection.getJarFileURL());
        }
        if ("file".equals(resource.getProtocol())) {
            try {
                Path path = Path.of(resource.toURI());
                for (int i = 0; i < resourcePath.split("/").length; i++) {
                    path = path.getParent();
                    if (path == null) {
                        return Optional.empty();
                    }
                }
                return Optional.of(path.toUri().toURL());
            } catch (URISyntaxException e) {
                throw new IOException("Invalid Minecraft adapter resource URL: " + resource, e);
            }
        }
        return Optional.empty();
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
        int total = 0;
        for (ModContainer mod : mods) {
            total += mod.descriptor().declarations("entrypoint").size();
        }
        events.post(new NowsEntrypointsStartingEvent(context));
        for (ModContainer mod : mods) {
            for (String className : mod.descriptor().declarations("entrypoint")) {
                LOG.info("Running entrypoint: {} -> {}", mod.descriptor().id(), className);
                events.post(new NowsModEntrypointStartingEvent(context, mod, className));
                NowsLoadingState.detail(mod.descriptor().id() + " -> " + className);
                NowsLoadingState.subtask("Mod entrypoints", count, total);
                Object instance = Class.forName(className, true, loader).getDeclaredConstructor().newInstance();
                if (!(instance instanceof ModInitializer initializer)) {
                    throw new IllegalStateException(className + " does not implement " + ModInitializer.class.getName());
                }
                initializer.onInitialize(context);
                count++;
                NowsLoadingState.subtask("Mod entrypoints", count, total);
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
        NowsLoadingState.begin(name);
        try {
            T result = action.get();
            phase.close();
            NowsLoadingState.complete(name);
            return result;
        } catch (Exception | Error failure) {
            phase.fail(failure);
            NowsLoadingState.fail(name, failure);
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
