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

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import space.nows.mcnows.mc.api.client.ui.RenderContext;
import space.nows.mcnows.mc.api.client.ui.ScreenContext;
import space.nows.mcnows.mc.api.client.ui.ScreenInitializer;
import space.nows.mcnows.mc.api.client.ui.ScreenRenderer;

public final class SimpleScreen extends Screen {
    private final ScreenInitializer initializer;
    private final ScreenRenderer renderer;

    public SimpleScreen(Component title, ScreenInitializer initializer, ScreenRenderer renderer) {
        super(title);
        this.initializer = initializer;
        this.renderer = renderer;
    }

    @Override
    protected void init() {
        initializer.init(new ScreenContext(
                width,
                height,
                (x, y, buttonWidth, buttonHeight, message, onPress) -> addRenderableWidget(
                        net.minecraft.client.gui.components.Button.builder(
                                        Component.literal(message),
                                        button -> onPress.run())
                                .bounds(x, y, buttonWidth, buttonHeight)
                                .build()),
                (title, nextInitializer, nextRenderer) -> minecraft.setScreenAndShow(
                        new SimpleScreen(Component.literal(title), nextInitializer, nextRenderer)),
                () -> minecraft.setScreenAndShow(null)));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        renderer.render(new RenderContext(
                graphics.guiWidth(),
                graphics.guiHeight(),
                mouseX,
                mouseY,
                delta,
                new GuiGraphicsExtractorSink(graphics)));
    }

    private record GuiGraphicsExtractorSink(GuiGraphicsExtractor graphics)
            implements space.nows.mcnows.mc.api.client.ui.RenderSink {
        @Override
        public void fill(int x1, int y1, int x2, int y2, int color) {
            graphics.fill(x1, y1, x2, y2, color);
        }

        @Override
        public void text(String text, int x, int y, int color) {
            graphics.text(net.minecraft.client.Minecraft.getInstance().font, text, x, y, color, true);
        }

        @Override
        public void centeredText(String text, int x, int y, int color) {
            graphics.centeredText(net.minecraft.client.Minecraft.getInstance().font, Component.literal(text), x, y, color);
        }

        @Override
        public void icon(String id, int x, int y, int width, int height) {
            graphics.blit(Identifier.parse(id), x, y, width, height, 0, 0, width, height);
        }
    }
}
