package space.nows.mcnows.mc.api.client.ui;

public interface TitleScreenUi {
    void addButton(TitleButtonFactory factory);

    void render(ScreenRenderer renderer);
}
