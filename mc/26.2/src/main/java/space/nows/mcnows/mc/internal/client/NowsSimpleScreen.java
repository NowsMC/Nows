package space.nows.mcnows.mc.internal.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
        initializer.init(new NowsScreenContext(this, this::addRenderableWidget));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        renderer.render(new NowsRenderContext(this, graphics, mouseX, mouseY, delta));
    }
}
