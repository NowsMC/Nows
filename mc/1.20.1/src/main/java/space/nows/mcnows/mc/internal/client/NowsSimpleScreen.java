package space.nows.mcnows.mc.internal.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import space.nows.mcnows.mc.api.client.ui.NowsButtonSink;
import space.nows.mcnows.mc.api.client.ui.NowsRenderContext;
import space.nows.mcnows.mc.api.client.ui.NowsScreenContext;
import space.nows.mcnows.mc.api.client.ui.NowsScreenInitializer;
import space.nows.mcnows.mc.api.client.ui.NowsScreenRenderer;

public final class NowsSimpleScreen extends Screen {
    private final NowsScreenInitializer initializer;
    private final NowsScreenRenderer renderer;

    public NowsSimpleScreen(Component title, NowsScreenInitializer initializer, NowsScreenRenderer renderer) {
        super(title);
        this.initializer = initializer;
        this.renderer = renderer;
    }

    @Override
    protected void init() {
        initializer.init(new NowsScreenContext(
                width,
                height,
                new NowsButtonSink() {
                    @Override
                    public void addButton(int x, int y, int buttonWidth, int buttonHeight, String message, Runnable onPress) {
                        addRenderableWidget(Button.builder(Component.literal(message), button -> onPress.run())
                                .bounds(x, y, buttonWidth, buttonHeight)
                                .build());
                    }

                    @Override
                    public void addIconButton(
                            int x, int y, int buttonWidth, int buttonHeight, String icon, String message, Runnable onPress) {
                        addRenderableWidget(new NowsIconButton(
                                x,
                                y,
                                buttonWidth,
                                buttonHeight,
                                ResourceLocation.tryParse(icon),
                                Component.literal(message),
                                onPress));
                    }
                },
                (title, nextInitializer, nextRenderer) -> minecraft.setScreen(
                        new NowsSimpleScreen(Component.literal(title), nextInitializer, nextRenderer)),
                () -> minecraft.setScreen(null)));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        renderer.render(new NowsRenderContext(
                graphics.guiWidth(),
                graphics.guiHeight(),
                mouseX,
                mouseY,
                delta,
                new NowsGuiGraphicsSink(graphics)));
    }

    private record NowsGuiGraphicsSink(GuiGraphics graphics)
            implements space.nows.mcnows.mc.api.client.ui.NowsRenderSink {
        @Override
        public void fill(int x1, int y1, int x2, int y2, int color) {
            graphics.fill(x1, y1, x2, y2, color);
        }

        @Override
        public void text(String text, int x, int y, int color) {
            graphics.drawString(net.minecraft.client.Minecraft.getInstance().font, text, x, y, color, true);
        }

        @Override
        public void centeredText(String text, int x, int y, int color) {
            graphics.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font, Component.literal(text), x, y, color);
        }

        @Override
        public void icon(String id, int x, int y, int width, int height) {
            graphics.blit(ResourceLocation.tryParse(id), x, y, 0, 0, width, height, width, height);
        }
    }
}
