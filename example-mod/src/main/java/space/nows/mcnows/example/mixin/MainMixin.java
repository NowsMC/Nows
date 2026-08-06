package space.nows.mcnows.example.mixin;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Small end-to-end proof that Nows hosts Mixin without a Java agent. */
@Mixin(value = Main.class, remap = false)
public abstract class MainMixin {
    @Inject(method = "main([Ljava/lang/String;)V", at = @At("HEAD"), remap = false)
    private static void nows$beforeMinecraftMain(String[] args, CallbackInfo ci) {
        System.out.println("[Nows Example/Mixin] net.minecraft.client.main.Main is being transformed by Nows");
    }
}
