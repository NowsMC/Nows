package space.nows.mcnows.mc.internal.client;

import space.nows.mcnows.mc.api.client.ui.RenderContext;
import space.nows.mcnows.mc.api.client.ui.ScreenContext;
import space.nows.mcnows.mc.api.client.ui.ScreenRenderer;
import space.nows.mcnows.mc.api.client.ui.TitleButtonFactory;
import space.nows.mcnows.mc.api.client.ui.TitleScreenUi;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class TitleScreenUiImpl implements TitleScreenUi {
    private final List<TitleButtonFactory> buttonFactories = new CopyOnWriteArrayList<>();
    private final List<ScreenRenderer> renderers = new CopyOnWriteArrayList<>();

    @Override
    public void addButton(TitleButtonFactory factory) {
        buttonFactories.add(factory);
    }

    @Override
    public void render(ScreenRenderer renderer) {
        renderers.add(renderer);
    }

    public void addButtons(ScreenContext context) {
        for (TitleButtonFactory factory : buttonFactories) {
            factory.add(context);
        }
    }

    public void renderAll(RenderContext context) {
        for (ScreenRenderer renderer : renderers) {
            renderer.render(context);
        }
    }
}
