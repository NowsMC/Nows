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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.nows.mc.api.client.ui.RenderContext;
import space.nows.mc.internal.client.UiImpl;

@Mixin(value = Gui.class, remap = false)
public final class GuiMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;F)V", at = @At("TAIL"), remap = false)
    private void nows$renderOverlays(PoseStack graphics, float delta, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        UiImpl.INSTANCE.renderOverlays(new RenderContext(
                minecraft.getWindow().getGuiScaledWidth(),
                minecraft.getWindow().getGuiScaledHeight(),
                -1,
                -1,
                delta,
                new OverlayRenderSink(graphics)));
    }

    private record OverlayRenderSink(PoseStack graphics)
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
