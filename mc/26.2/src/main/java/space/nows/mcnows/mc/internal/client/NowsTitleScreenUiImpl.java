package space.nows.mcnows.mc.internal.client;

import space.nows.mcnows.mc.api.client.ui.NowsRenderContext;
import space.nows.mcnows.mc.api.client.ui.NowsScreenContext;
import space.nows.mcnows.mc.api.client.ui.NowsScreenRenderer;
import space.nows.mcnows.mc.api.client.ui.NowsTitleButtonFactory;
import space.nows.mcnows.mc.api.client.ui.NowsTitleScreenUi;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class NowsTitleScreenUiImpl implements NowsTitleScreenUi {
    private final List<NowsTitleButtonFactory> buttonFactories = new CopyOnWriteArrayList<>();
    private final List<NowsScreenRenderer> renderers = new CopyOnWriteArrayList<>();

    @Override
    public void addButton(NowsTitleButtonFactory factory) {
        buttonFactories.add(factory);
    }

    @Override
    public void render(NowsScreenRenderer renderer) {
        renderers.add(renderer);
    }

    public void addButtons(NowsScreenContext context) {
        for (NowsTitleButtonFactory factory : buttonFactories) {
            context.addWidget(factory.create(context));
        }
    }

    public void renderAll(NowsRenderContext context) {
        for (NowsScreenRenderer renderer : renderers) {
            renderer.render(context);
        }
    }
}
