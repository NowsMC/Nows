package space.nows.mcnows.mc.internal.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class IconButton extends Button {
    private final Identifier icon;

    public IconButton(int x, int y, int width, int height, Identifier icon, Component message, Runnable onPress) {
        super(x, y, width, height, message, button -> onPress.run(), DEFAULT_NARRATION);
        this.icon = icon;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        extractDefaultSprite(graphics);
        int size = Math.min(16, Math.min(getWidth(), getHeight()) - 4);
        int x = getX() + (getWidth() - size) / 2;
        int y = getY() + (getHeight() - size) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, icon, x, y, 0, 0, size, size, size, size);
    }
}
