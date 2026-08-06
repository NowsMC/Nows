package space.nows.mcnows.mixin;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import space.nows.mcnows.core.classloading.NowsClassLoader;
import space.nows.mcnows.core.mod.ModContainer;
import space.nows.mcnows.integration.logging.NowsLog;

import java.util.List;

public final class NowsMixinBootstrap {
    private NowsMixinBootstrap() {
    }

    public static void install(NowsClassLoader loader, List<ModContainer> mods) {
        NowsMixinService.attach(loader);
        Thread.currentThread().setContextClassLoader(loader);
        MixinBootstrap.init();

        for (ModContainer mod : mods) {
            for (String config : mod.descriptor().declarations("mixin")) {
                Mixins.addConfiguration(config);
                NowsLog.get(NowsMixinBootstrap.class).info("Mixin config: {} -> {}", mod.descriptor().id(), config);
            }
        }

        IMixinTransformer transformer = NowsMixinService.transformer();
        if (transformer == null) {
            throw new IllegalStateException("Mixin did not provide an IMixinTransformer to the Nows service");
        }

        loader.addTransformerFirst((className, classBytes) ->
                transformer.transformClassBytes(className, className, classBytes));
        loader.setClassGenerator(className ->
                transformer.generateClass(MixinEnvironment.getCurrentEnvironment(), className));
    }
}
