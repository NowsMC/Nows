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

package space.nows.mc.internal.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class IconButton extends Button {
    private final Identifier icon;

    public IconButton(int x, int y, int width, int height, Identifier icon, Component message, Runnable onPress) {
        super(x, y, width, height, Component.empty(), button -> onPress.run(), DEFAULT_NARRATION);
        this.icon = icon;
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderDefaultSprite(graphics);
        int size = Math.min(16, Math.min(getWidth(), getHeight()) - 4);
        int x = getX() + (getWidth() - size) / 2;
        int y = getY() + (getHeight() - size) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, icon, x, y, 0, 0, size, size, size, size);
    }
}
