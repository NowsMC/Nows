package space.nows.mcnows.mc.api.client.ui;

public final class ScreenContext {
    private final int width;
    private final int height;
    private final ButtonSink buttons;
    private final SimpleScreenSink screens;
    private final Runnable close;

    public ScreenContext(int width, int height, ButtonSink buttons, SimpleScreenSink screens, Runnable close) {
        this.width = width;
        this.height = height;
        this.buttons = buttons;
        this.screens = screens;
        this.close = close;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int centerX(int width) {
        return (width() - width) / 2;
    }

    public int centerY(int height) {
        return (height() - height) / 2;
    }

    public void addButton(int x, int y, int width, int height, String message, Runnable onPress) {
        buttons.addButton(x, y, width, height, message, onPress);
    }

    public void addIconButton(int x, int y, int width, int height, String icon, String message, Runnable onPress) {
        buttons.addIconButton(x, y, width, height, icon, message, onPress);
    }

    public void showSimpleScreen(String title, ScreenInitializer initializer, ScreenRenderer renderer) {
        screens.showSimpleScreen(title, initializer, renderer);
    }

    public void close() {
        close.run();
    }
}
