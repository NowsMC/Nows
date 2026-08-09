package space.nows.mcnows.mc.api.client.ui;

@FunctionalInterface
public interface NowsButtonSink {
    void addButton(int x, int y, int width, int height, String message, Runnable onPress);
}
