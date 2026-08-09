package space.nows.mcnows.mc.internal.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class NowsIconButton extends Button {
    private final ResourceLocation icon;

    public NowsIconButton(int x, int y, int width, int height, ResourceLocation icon, Component message, Runnable onPress) {
        super(x, y, width, height, message, button -> onPress.run(), DEFAULT_NARRATION);
        this.icon = icon;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderWidget(graphics, mouseX, mouseY, delta);
        int size = Math.min(16, Math.min(getWidth(), getHeight()) - 4);
        int x = getX() + (getWidth() - size) / 2;
        int y = getY() + (getHeight() - size) / 2;
        graphics.blit(icon, x, y, 0, 0, size, size, size, size);
    }
}
