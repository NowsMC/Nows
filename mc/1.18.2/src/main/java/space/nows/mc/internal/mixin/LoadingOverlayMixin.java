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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.nows.platform.core.loading.NowsLoadingSnapshot;
import space.nows.mc.internal.ClientHooks;

@Mixin(value = LoadingOverlay.class, remap = false)
public abstract class LoadingOverlayMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", at = @At("TAIL"), remap = false)
    private void nows$renderLoadingDetails(PoseStack graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        NowsLoadingSnapshot loading = ClientHooks.loadingSnapshot();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int width = Math.max(180, Math.min(360, screenWidth - 48));
        int x = (screenWidth - width) / 2;
        int y = Math.max(24, screenHeight - 58);

        GuiComponent.drawString(graphics, minecraft.font, loading.title() + " - " + loading.stage(), x, y, loading.failed() ? 0xFFFF8080 : 0xFFFFFFFF);
        GuiComponent.drawString(graphics, minecraft.font, loading.step() + " / " + loading.totalSteps(), x + width - 48, y, 0xFFE8E8E8);
        drawBar(graphics, x, y + 13, width, loading.progress(), loading.failed() ? 0xFFFF8080 : 0xFFFFFFFF);
        if (!loading.detail().isBlank()) {
            GuiComponent.drawString(graphics, minecraft.font, loading.detail(), x, y + 19, 0xFFD8D8D8);
        }
        if (loading.subTotal() > 0) {
            GuiComponent.drawString(graphics, minecraft.font, loading.subtask() + " " + loading.subStep() + " / " + loading.subTotal(), x, y + 31, 0xFFE8E8E8);
            drawBar(graphics, x, y + 44, width, loading.subProgress(), 0xFFE8E8E8);
        }
    }

    private static void drawBar(PoseStack graphics, int x, int y, int width, float progress, int color) {
        GuiComponent.fill(graphics, x, y, x + width, y + 2, 0x55FFFFFF);
        GuiComponent.fill(graphics, x, y, x + Math.max(1, Math.round(width * progress)), y + 2, color);
    }
}
