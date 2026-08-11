package space.nows.mcnows.mc.internal.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.nows.mcnows.mc.internal.event.GameEventsImpl;

@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftClientMixin {
    @Inject(method = "tick()V", at = @At("TAIL"), remap = false)
    private void nows$tickClient(CallbackInfo ci) {
        GameEventsImpl.INSTANCE.dispatchClientTick((Minecraft) (Object) this);
    }
}
