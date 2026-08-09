package space.nows.mcnows.mc.internal.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.nows.mcnows.mc.internal.NowsMinecraftClientHooks;

import java.util.concurrent.CompletableFuture;

@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftMixin {
    @Shadow
    @Final
    private PackRepository resourcePackRepository;

    @Shadow
    public abstract CompletableFuture<Void> reloadResourcePacks();

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void nows$installModResourcePacks(CallbackInfo ci) {
        if (NowsMinecraftClientHooks.installResourcePacks(resourcePackRepository)) {
            reloadResourcePacks();
        }
    }
}
