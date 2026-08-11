package space.nows.mcnows.mc.api.client.ui;

public interface RenderSink {
    void fill(int x1, int y1, int x2, int y2, int color);

    void text(String text, int x, int y, int color);

    void centeredText(String text, int x, int y, int color);

    void icon(String id, int x, int y, int width, int height);
}
