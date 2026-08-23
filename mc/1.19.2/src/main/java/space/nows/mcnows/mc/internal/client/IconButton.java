/*
 * Copyright 2026 TamKungZ_ (Nows MC — https://nows.space)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
