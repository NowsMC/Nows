package space.nows.mcnows.mc.internal.mixin;

import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.nows.mcnows.mc.internal.ClientHooks;

import java.util.Set;

@Mixin(value = PackRepository.class, remap = false)
public abstract class PackRepositoryMixin {
    @Shadow
    @Final
    @Mutable
    private Set<RepositorySource> sources;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void nows$installModResourcePackSource(RepositorySource[] repositorySources, CallbackInfo ci) {
        sources = ClientHooks.withModResourcePackSource(sources, repositorySources);
    }
}
