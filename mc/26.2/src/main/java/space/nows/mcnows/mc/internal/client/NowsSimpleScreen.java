package space.nows.mcnows.mc.internal.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
                (x, y, buttonWidth, buttonHeight, message, onPress) -> addRenderableWidget(
                        net.minecraft.client.gui.components.Button.builder(
                                        Component.literal(message),
                                        button -> onPress.run())
                                .bounds(x, y, buttonWidth, buttonHeight)
                                .build()),
                (title, nextInitializer, nextRenderer) -> minecraft.setScreenAndShow(
                        new NowsSimpleScreen(Component.literal(title), nextInitializer, nextRenderer)),
                () -> minecraft.setScreenAndShow(null)));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        renderer.render(new NowsRenderContext(
                graphics.guiWidth(),
                graphics.guiHeight(),
                mouseX,
                mouseY,
                delta,
                new NowsGuiGraphicsExtractorSink(graphics)));
    }

    private record NowsGuiGraphicsExtractorSink(GuiGraphicsExtractor graphics)
            implements space.nows.mcnows.mc.api.client.ui.NowsRenderSink {
        @Override
        public void fill(int x1, int y1, int x2, int y2, int color) {
            graphics.fill(x1, y1, x2, y2, color);
        }

        @Override
        public void text(String text, int x, int y, int color) {
            graphics.text(net.minecraft.client.Minecraft.getInstance().font, text, x, y, color, true);
        }

        @Override
        public void centeredText(String text, int x, int y, int color) {
            graphics.centeredText(net.minecraft.client.Minecraft.getInstance().font, Component.literal(text), x, y, color);
        }

        @Override
        public void icon(String id, int x, int y, int width, int height) {
            graphics.blit(Identifier.parse(id), x, y, width, height, 0, 0, width, height);
        }
    }
}
