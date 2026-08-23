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

package space.nows.mc.internal.mixin;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.nows.mc.api.client.ui.RenderContext;
import space.nows.mc.api.client.ui.ButtonSink;
import space.nows.mc.api.client.ui.ScreenContext;
import space.nows.mc.internal.ClientHooks;
import space.nows.mc.internal.client.IconButton;
import space.nows.mc.internal.client.UiImpl;

@Mixin(value = TitleScreen.class, remap = false)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("TAIL"), remap = false)
    private void nows$initTitleUi(CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        UiImpl.INSTANCE.titleScreenImpl().addButtons(
                new ScreenContext(
                        screen.width,
                        screen.height,
                        new ButtonSink() {
                            @Override
                            public void addButton(int x, int y, int width, int height, String message, Runnable onPress) {
                                addRenderableWidget(Button.builder(Component.literal(message), button -> onPress.run())
                                        .bounds(x, y, width, height)
                                        .build());
                            }

                            @Override
                            public void addIconButton(
                                    int x, int y, int width, int height, String icon, String message, Runnable onPress) {
                                addRenderableWidget(new IconButton(
                                        x,
                                        y,
                                        width,
                                        height,
                                        ResourceLocation.tryParse(icon),
                                        Component.literal(message),
                                        onPress));
                            }
                        },
                        (title, initializer, renderer) -> Minecraft.getInstance().setScreen(
                                new space.nows.mc.internal.client.SimpleScreen(
                                        Component.literal(title), initializer, renderer)),
                        () -> Minecraft.getInstance().setScreen(null),
                        context -> Minecraft.getInstance().setScreen(
                                new space.nows.mc.internal.client.ModListScreen(screen, context))));
    }

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V",
            at = @At("TAIL"),
            remap = false
    )
    private void nows$renderTitleBadge(PoseStack graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        int x = 2;
        int y = height - 30;
        GuiComponent.drawString(graphics, minecraft.font, ClientHooks.loaderLine(), x, y, 0xFFFFFFFF);
        GuiComponent.drawString(graphics, minecraft.font, ClientHooks.modLine(), x, y + 10, 0xFFB8B8B8);
        UiImpl.INSTANCE.titleScreenImpl().renderAll(
                new RenderContext(
                        width,
                        height,
                        mouseX,
                        mouseY,
                        delta,
                        new TitleRenderSink(graphics)));
    }

    private record TitleRenderSink(PoseStack graphics)
            implements space.nows.mc.api.client.ui.RenderSink {
        @Override
        public void fill(int x1, int y1, int x2, int y2, int color) {
            GuiComponent.fill(graphics, x1, y1, x2, y2, color);
        }

        @Override
        public void text(String text, int x, int y, int color) {
            GuiComponent.drawString(graphics, Minecraft.getInstance().font, text, x, y, color);
        }

        @Override
        public void centeredText(String text, int x, int y, int color) {
            GuiComponent.drawCenteredString(graphics, Minecraft.getInstance().font, Component.literal(text), x, y, color);
        }

        @Override
        public void icon(String id, int x, int y, int width, int height) {
            ResourceLocation texture = ResourceLocation.tryParse(id);
            if (texture == null) {
                return;
            }
            RenderSystem.setShaderTexture(0, texture);
            GuiComponent.blit(graphics, x, y, 0, 0, width, height, width, height);
        }
    }
}
