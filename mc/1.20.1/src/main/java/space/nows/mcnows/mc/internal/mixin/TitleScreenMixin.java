package space.nows.mcnows.mc.internal.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.nows.mcnows.mc.api.client.ui.NowsRenderContext;
import space.nows.mcnows.mc.api.client.ui.NowsButtonSink;
import space.nows.mcnows.mc.api.client.ui.NowsScreenContext;
import space.nows.mcnows.mc.internal.NowsMinecraftClientHooks;
import space.nows.mcnows.mc.internal.client.NowsIconButton;
import space.nows.mcnows.mc.internal.client.NowsUiImpl;

@Mixin(value = TitleScreen.class, remap = false)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("TAIL"), remap = false)
    private void nows$initTitleUi(CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        NowsUiImpl.INSTANCE.titleScreenImpl().addButtons(
                new NowsScreenContext(
                        screen.width,
                        screen.height,
                        new NowsButtonSink() {
                            @Override
                            public void addButton(int x, int y, int width, int height, String message, Runnable onPress) {
                                addRenderableWidget(Button.builder(Component.literal(message), button -> onPress.run())
                                        .bounds(x, y, width, height)
                                        .build());
                            }

                            @Override
                            public void addIconButton(
                                    int x, int y, int width, int height, String icon, String message, Runnable onPress) {
                                addRenderableWidget(new NowsIconButton(
                                        x,
                                        y,
                                        width,
                                        height,
                                        ResourceLocation.tryParse(icon),
                                        Component.literal(message),
                                        onPress));
                            }
                        },
                        (title, initializer, renderer) -> Minecraft.getInstance().setScreen(
                                new space.nows.mcnows.mc.internal.client.NowsSimpleScreen(
                                        Component.literal(title), initializer, renderer)),
                        () -> Minecraft.getInstance().setScreen(null)));
    }

    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I",
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private void nows$renderTitleBadge(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        int x = 2;
        int y = graphics.guiHeight() - 30;
        graphics.drawString(minecraft.font, NowsMinecraftClientHooks.loaderLine(), x, y, 0xFFFFFFFF, true);
        graphics.drawString(minecraft.font, NowsMinecraftClientHooks.modLine(), x, y + 10, 0xFFB8B8B8, true);
        NowsUiImpl.INSTANCE.titleScreenImpl().renderAll(
                new NowsRenderContext(
                        graphics.guiWidth(),
                        graphics.guiHeight(),
                        mouseX,
                        mouseY,
                        delta,
                        new NowsTitleRenderSink(graphics)));
    }

    private record NowsTitleRenderSink(GuiGraphics graphics)
            implements space.nows.mcnows.mc.api.client.ui.NowsRenderSink {
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
            graphics.blit(ResourceLocation.tryParse(id), x, y, 0, 0, width, height, width, height);
        }
    }
}
