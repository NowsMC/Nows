package space.nows.mcnows.mc.api.client.ui;

public final class NowsScreenContext {
    private final int width;
    private final int height;
    private final NowsButtonSink buttons;
    private final NowsSimpleScreenSink screens;
    private final Runnable close;

    public NowsScreenContext(int width, int height, NowsButtonSink buttons, NowsSimpleScreenSink screens, Runnable close) {
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

    public void showSimpleScreen(String title, NowsScreenInitializer initializer, NowsScreenRenderer renderer) {
        screens.showSimpleScreen(title, initializer, renderer);
    }

    public void close() {
        close.run();
    }
}
