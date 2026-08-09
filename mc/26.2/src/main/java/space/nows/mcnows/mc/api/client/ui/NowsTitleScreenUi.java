package space.nows.mcnows.mc.api.client.ui;

public interface NowsTitleScreenUi {
    void addButton(NowsTitleButtonFactory factory);

    void render(NowsScreenRenderer renderer);
}
