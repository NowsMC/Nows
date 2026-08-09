package space.nows.mcnows.mc.api.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class NowsRenderContext {
    private final Screen screen;
    private final GuiGraphicsExtractor graphics;
    private final int mouseX;
    private final int mouseY;
    private final float delta;

    public NowsRenderContext(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        this.screen = screen;
        this.graphics = graphics;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.delta = delta;
    }

    public Screen screen() {
        return screen;
    }

    public GuiGraphicsExtractor graphics() {
        return graphics;
    }

    public Font font() {
        return Minecraft.getInstance().font;
    }

    public int width() {
        return graphics.guiWidth();
    }

    public int height() {
        return graphics.guiHeight();
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
        graphics.fill(x1, y1, x2, y2, color);
    }

    public void text(String text, int x, int y, int color) {
        graphics.text(font(), text, x, y, color, true);
    }

    public void text(Component text, int x, int y, int color) {
        graphics.text(font(), text, x, y, color, true);
    }

    public void centeredText(Component text, int x, int y, int color) {
        graphics.centeredText(font(), text, x, y, color);
    }
}
