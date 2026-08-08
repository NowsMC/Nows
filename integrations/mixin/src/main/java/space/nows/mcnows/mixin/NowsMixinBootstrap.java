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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class NowsMixinBootstrap {
    private static final Logger LOG = NowsLog.get(NowsMixinBootstrap.class);

    private NowsMixinBootstrap() {
    }

    public static void install(NowsClassLoader loader, List<ModContainer> mods) {
        NowsMixinService.attach(loader);
        Thread.currentThread().setContextClassLoader(loader);
        MixinBootstrap.init();

        Set<String> configs = new LinkedHashSet<>();
        for (ModContainer mod : mods) {
            for (String config : mod.descriptor().declarations("mixin")) {
                validateMixinConfig(loader, mod, config);
                if (!configs.add(config)) {
                    LOG.warn("Duplicate Mixin config declaration ignored after first registration: {}", config);
                    continue;
                }
                Mixins.addConfiguration(config);
                LOG.info("Mixin config: {} -> {}", mod.descriptor().id(), config);
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
                transformer.generateClass(MixinEnvironment.getCurrentEnvironment(), className));
        LOG.debug("Mixin transformer installed into NowsClassLoader");
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
}
