package space.nows.mcnows.mc.internal.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.nows.mcnows.mc.api.client.ui.NowsRenderContext;
import space.nows.mcnows.mc.api.client.ui.NowsScreenContext;
import space.nows.mcnows.mc.internal.NowsMinecraftClientHooks;
import space.nows.mcnows.mc.internal.client.NowsUiImpl;

@Mixin(value = TitleScreen.class, remap = false)
public abstract class TitleScreenMixin {
    @Inject(method = "init()V", at = @At("TAIL"), remap = false)
    private void nows$initTitleUi(CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        ScreenAccessor accessor = (ScreenAccessor) this;
        NowsUiImpl.INSTANCE.titleScreenImpl().addButtons(
                new NowsScreenContext(
                        screen.width,
                        screen.height,
                        (x, y, width, height, message, onPress) -> accessor.nows$addRenderableWidget(
                                Button.builder(Component.literal(message), button -> onPress.run())
                                        .bounds(x, y, width, height)
                                        .build()),
                        (title, initializer, renderer) -> Minecraft.getInstance().setScreenAndShow(
                                new space.nows.mcnows.mc.internal.client.NowsSimpleScreen(
                                        Component.literal(title), initializer, renderer)),
                        () -> Minecraft.getInstance().setScreenAndShow(null)));
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V",
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private void nows$extractTitleBadge(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        int x = 2;
        int y = graphics.guiHeight() - 30;
        graphics.text(minecraft.font, NowsMinecraftClientHooks.loaderLine(), x, y, 0xFFFFFFFF, true);
        graphics.text(minecraft.font, NowsMinecraftClientHooks.modLine(), x, y + 10, 0xFFB8B8B8, true);
        NowsUiImpl.INSTANCE.titleScreenImpl().renderAll(
                new NowsRenderContext(
                        graphics.guiWidth(),
                        graphics.guiHeight(),
                        mouseX,
                        mouseY,
                        delta,
                        new NowsTitleRenderSink(graphics)));
    }

    private record NowsTitleRenderSink(GuiGraphicsExtractor graphics)
            implements space.nows.mcnows.mc.api.client.ui.NowsRenderSink {
        @Override
        public void fill(int x1, int y1, int x2, int y2, int color) {
            graphics.fill(x1, y1, x2, y2, color);
        }

        @Override
        public void text(String text, int x, int y, int color) {
            graphics.text(Minecraft.getInstance().font, text, x, y, color, true);
        }

        @Override
        public void centeredText(String text, int x, int y, int color) {
            graphics.centeredText(Minecraft.getInstance().font, Component.literal(text), x, y, color);
        }

        @Override
        public void icon(String id, int x, int y, int width, int height) {
            graphics.blit(Identifier.parse(id), x, y, width, height, 0, 0, width, height);
        }
    }
}
