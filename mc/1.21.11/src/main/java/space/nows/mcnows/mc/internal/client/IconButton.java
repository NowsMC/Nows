package space.nows.mcnows.mc.internal.client;

import net.minecraft.client.gui.GuiGraphics;
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
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderDefaultLabel(graphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.TOOLTIP_AND_CURSOR));
        int size = Math.min(16, Math.min(getWidth(), getHeight()) - 4);
        int x = getX() + (getWidth() - size) / 2;
        int y = getY() + (getHeight() - size) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, icon, x, y, 0, 0, size, size, size, size);
    }
}
