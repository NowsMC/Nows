package space.nows.mcnows.mc.internal.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.nows.mcnows.mc.internal.NowsMinecraftClientHooks;

@Mixin(value = TitleScreen.class, remap = false)
public abstract class TitleScreenMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("TAIL"), remap = false)
    private void nows$extractTitleBadge(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        graphics.text(minecraft.font, NowsMinecraftClientHooks.brandLine(), 2, 2, 0xFFFFFFFF, true);
        graphics.text(minecraft.font, NowsMinecraftClientHooks.modLine(), 2, 12, 0xFFB8B8B8, true);
    }
}
