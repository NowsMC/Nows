package space.nows.mcnows.mc.api.client.ui;

@FunctionalInterface
public interface ButtonSink {
    void addButton(int x, int y, int width, int height, String message, Runnable onPress);

    default void addIconButton(int x, int y, int width, int height, String icon, String message, Runnable onPress) {
        addButton(x, y, width, height, message, onPress);
    }
}
