package space.nows.mcnows.example.mixin;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import reactor.util.Logger;
import space.nows.mcnows.integration.logging.NowsLog;

/** Small end-to-end proof that Nows hosts Mixin without a Java agent. */
@Mixin(value = Main.class, remap = false)
public abstract class MainMixin {
    private static final Logger LOG = NowsLog.get(MainMixin.class);

    @Inject(method = "main([Ljava/lang/String;)V", at = @At("HEAD"), remap = false)
    private static void nows$beforeMinecraftMain(String[] args, CallbackInfo ci) {
        LOG.info("Nows Example/Mixin: net.minecraft.client.main.Main is being transformed by Nows");
    }
}
