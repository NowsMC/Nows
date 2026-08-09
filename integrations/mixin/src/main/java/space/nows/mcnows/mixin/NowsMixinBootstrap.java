package space.nows.mcnows.mixin;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import reactor.util.Logger;
import space.nows.mcnows.core.classloading.NowsClassLoader;
import space.nows.mcnows.core.mod.ModContainer;
import space.nows.mcnows.integration.logging.NowsLog;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class NowsMixinBootstrap {
    private static final Logger LOG = NowsLog.get(NowsMixinBootstrap.class);

    private NowsMixinBootstrap() {
    }

    public static void install(NowsClassLoader loader, List<ModContainer> mods) {
        install(loader, List.of(), mods);
    }

    public static void install(NowsClassLoader loader, List<String> builtInConfigs, List<ModContainer> mods) {
        NowsMixinService.attach(loader);
        Thread.currentThread().setContextClassLoader(loader);
        LOG.info("Bootstrapping Mixin with context classloader {}", loader.getName());
        MixinBootstrap.init();
        MixinEnvironment currentEnvironment = MixinEnvironment.getCurrentEnvironment();
        MixinEnvironment defaultEnvironment = MixinEnvironment.getDefaultEnvironment();
        LOG.info("Mixin current environment: {} phase {} side {}",
                currentEnvironment, currentEnvironment.getPhase(), currentEnvironment.getSide());
        LOG.info("Mixin default environment: {} phase {} side {}",
                defaultEnvironment, defaultEnvironment.getPhase(), defaultEnvironment.getSide());

        Set<String> configs = new LinkedHashSet<>();
        for (String config : builtInConfigs) {
            validateBuiltInMixinConfig(loader, config);
            if (!configs.add(config)) {
                LOG.warn("Duplicate built-in Mixin config declaration ignored after first registration: {}", config);
                continue;
            }
            addConfiguration(currentEnvironment, config);
            LOG.info("Built-in Mixin config: {} ({})", config, currentEnvironment);
        }
        for (ModContainer mod : mods) {
            for (String config : mod.descriptor().declarations("mixin")) {
                validateMixinConfig(loader, mod, config);
                if (!configs.add(config)) {
                    LOG.warn("Duplicate Mixin config declaration ignored after first registration: {}", config);
                    continue;
                }
                addConfiguration(currentEnvironment, config);
                LOG.info("Mixin config: {} -> {} ({})", mod.descriptor().id(), config, currentEnvironment);
            }
        }
        LOG.info("Registered {} Mixin config(s)", configs.size());

        IMixinTransformer transformer = NowsMixinService.transformer();
        if (transformer == null) {
            throw new IllegalStateException("Mixin did not provide an IMixinTransformer to the Nows service");
        }

        loader.addTransformerFirst((className, classBytes) ->
                transformer.transformClassBytes(className, className, classBytes));
        loader.setClassGenerator(className ->
                transformer.generateClass(currentEnvironment, className));
        LOG.info("Mixin transformer installed into NowsClassLoader: {}", transformer.getClass().getName());
    }

    private static void addConfiguration(MixinEnvironment environment, String config) {
        try {
            Method addConfiguration = Mixins.class.getDeclaredMethod(
                    "addConfiguration", String.class, MixinEnvironment.class);
            addConfiguration.setAccessible(true);
            addConfiguration.invoke(null, config, environment);
        } catch (ReflectiveOperationException | RuntimeException failure) {
            LOG.warn("Falling back to deprecated MixinEnvironment.addConfiguration for {}", config, failure);
            environment.addConfiguration(config);
        }
    }

    public static void detach(NowsClassLoader loader) {
        NowsMixinService.detach(loader);
    }

    private static void validateMixinConfig(NowsClassLoader loader, ModContainer mod, String config) {
        if (config == null || config.isBlank()) {
            throw new IllegalArgumentException("Mod " + mod.descriptor().id() + " declares a blank Mixin config");
        }
        try (InputStream input = loader.getResourceAsStream(config)) {
            if (input == null) {
                throw new IllegalStateException("Mod " + mod.descriptor().id()
                        + " declares missing Mixin config " + config + " in " + mod.path());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read Mixin config " + config
                    + " declared by mod " + mod.descriptor().id() + " in " + mod.path(), e);
        }
    }

    private static void validateBuiltInMixinConfig(NowsClassLoader loader, String config) {
        if (config == null || config.isBlank()) {
            throw new IllegalArgumentException("Built-in Mixin config declaration is blank");
        }
        try (InputStream input = loader.getResourceAsStream(config)) {
            if (input == null) {
                throw new IllegalStateException("Missing built-in Mixin config " + config);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read built-in Mixin config " + config, e);
        }
    }
}
