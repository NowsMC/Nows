package space.nows.mcnows.mc.internal.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.nows.mcnows.mc.api.client.ui.RenderContext;
import space.nows.mcnows.mc.internal.ClientHooks;
import space.nows.mcnows.mc.internal.client.UiImpl;

@Mixin(value = Screen.class, remap = false)
public abstract class ScreenRenderMixin {
    @Inject(
            method = "renderWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At("TAIL"),
            remap = false
    )
    private void nows$renderTitleBadge(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!((Object) this instanceof TitleScreen)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        int x = 2;
        int y = graphics.guiHeight() - 30;
        graphics.drawString(minecraft.font, ClientHooks.loaderLine(), x, y, 0xFFFFFFFF, true);
        graphics.drawString(minecraft.font, ClientHooks.modLine(), x, y + 10, 0xFFB8B8B8, true);
        UiImpl.INSTANCE.titleScreenImpl().renderAll(
                new RenderContext(
                        graphics.guiWidth(),
                        graphics.guiHeight(),
                        mouseX,
                        mouseY,
                        delta,
                        new TitleRenderSink(graphics)));
    }

    private record TitleRenderSink(GuiGraphics graphics)
            implements space.nows.mcnows.mc.api.client.ui.RenderSink {
        @Override
        public void fill(int x1, int y1, int x2, int y2, int color) {
            graphics.fill(x1, y1, x2, y2, color);
        }

        @Override
        public void text(String text, int x, int y, int color) {
            graphics.drawString(Minecraft.getInstance().font, text, x, y, color, true);
        }

        @Override
        public void centeredText(String text, int x, int y, int color) {
            graphics.drawCenteredString(Minecraft.getInstance().font, Component.literal(text), x, y, color);
        }

        @Override
        public void icon(String id, int x, int y, int width, int height) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.tryParse(id), x, y, 0, 0, width, height, width, height);
        }
    }
}
