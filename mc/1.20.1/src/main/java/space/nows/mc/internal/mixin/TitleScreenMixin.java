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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
    @Shadow
    @Final
    private boolean fading;

    @Shadow
    private long fadeInStart;

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
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I",
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private void nows$renderTitleBadge(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        float alpha = nows$titleAlpha();
        if (alpha <= 0.0F) {
            return;
        }
        int x = 2;
        int y = graphics.guiHeight() - 30;
        graphics.drawString(minecraft.font, ClientHooks.loaderLine(), x, y, fadeColor(0xFFFFFFFF, alpha), true);
        graphics.drawString(minecraft.font, ClientHooks.modLine(), x, y + 10, fadeColor(0xFFB8B8B8, alpha), true);
        UiImpl.INSTANCE.titleScreenImpl().renderAll(
                new RenderContext(
                        graphics.guiWidth(),
                        graphics.guiHeight(),
                        mouseX,
                        mouseY,
                        delta,
                        new TitleRenderSink(graphics, alpha)));
    }

    private float nows$titleAlpha() {
        if (!fading) {
            return 1.0F;
        }
        if (fadeInStart == 0L) {
            fadeInStart = Util.getMillis();
        }
        return Mth.clamp((float) (Util.getMillis() - fadeInStart) / 1000.0F, 0.0F, 1.0F);
    }

    private static int fadeColor(int color, float alpha) {
        int baseAlpha = color >>> 24;
        int fadedAlpha = Mth.clamp(Math.round(baseAlpha * alpha), 0, 255);
        return (color & 0x00FFFFFF) | (fadedAlpha << 24);
    }

    private record TitleRenderSink(GuiGraphics graphics, float alpha)
            implements space.nows.mc.api.client.ui.RenderSink {
        @Override
        public void fill(int x1, int y1, int x2, int y2, int color) {
            graphics.fill(x1, y1, x2, y2, fadeColor(color, alpha));
        }

        @Override
        public void text(String text, int x, int y, int color) {
            graphics.drawString(Minecraft.getInstance().font, text, x, y, fadeColor(color, alpha), true);
        }

        @Override
        public void centeredText(String text, int x, int y, int color) {
            graphics.drawCenteredString(Minecraft.getInstance().font, Component.literal(text), x, y, fadeColor(color, alpha));
        }

        @Override
        public void icon(String id, int x, int y, int width, int height) {
            graphics.blit(ResourceLocation.tryParse(id), x, y, 0, 0, width, height, width, height);
        }
    }
}
