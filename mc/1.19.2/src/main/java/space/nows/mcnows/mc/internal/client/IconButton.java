package space.nows.mcnows.mc.internal.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class IconButton extends Button {
    private final ResourceLocation icon;

    public IconButton(int x, int y, int width, int height, ResourceLocation icon, Component message, Runnable onPress) {
        super(x, y, width, height, Component.empty(), button -> onPress.run());
        this.icon = icon;
    }

    @Override
    public void renderButton(PoseStack graphics, int mouseX, int mouseY, float delta) {
        super.renderButton(graphics, mouseX, mouseY, delta);
        int size = Math.min(16, Math.min(getWidth(), getHeight()) - 4);
        int x = this.x + (getWidth() - size) / 2;
        int y = this.y + (getHeight() - size) / 2;
        if (icon != null) {
            RenderSystem.setShaderTexture(0, icon);
            GuiComponent.blit(graphics, x, y, 0, 0, size, size, size, size);
        }
    }
}
