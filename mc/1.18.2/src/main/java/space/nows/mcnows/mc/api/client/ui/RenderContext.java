package space.nows.mcnows.mc.api.client.ui;

public final class RenderContext {
    private final int width;
    private final int height;
    private final int mouseX;
    private final int mouseY;
    private final float delta;
    private final RenderSink renderer;

    public RenderContext(int width, int height, int mouseX, int mouseY, float delta, RenderSink renderer) {
        this.width = width;
        this.height = height;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.delta = delta;
        this.renderer = renderer;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int mouseX() {
        return mouseX;
    }

    public int mouseY() {
        return mouseY;
    }

    public float delta() {
        return delta;
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        renderer.fill(x1, y1, x2, y2, color);
    }

    public void text(String text, int x, int y, int color) {
        renderer.text(text, x, y, color);
    }

    public void centeredText(String text, int x, int y, int color) {
        renderer.centeredText(text, x, y, color);
    }

    public void icon(String id, int x, int y, int width, int height) {
        renderer.icon(id, x, y, width, height);
    }
}
