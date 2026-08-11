package space.nows.mcnows.mc.internal.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.nows.mcnows.mc.internal.event.GameEventsImpl;

import java.util.function.BooleanSupplier;

@Mixin(value = MinecraftServer.class, remap = false)
public abstract class MinecraftServerMixin {
    @Inject(method = "tickServer(Ljava/util/function/BooleanSupplier;)V", at = @At("TAIL"), remap = false)
    private void nows$tickServer(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        GameEventsImpl.INSTANCE.dispatchServerTick((MinecraftServer) (Object) this);
    }
}
