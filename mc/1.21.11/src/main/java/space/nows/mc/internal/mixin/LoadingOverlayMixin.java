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
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.nows.platform.core.loading.NowsLoadingSnapshot;
import space.nows.mc.internal.ClientHooks;

@Mixin(value = LoadingOverlay.class, remap = false)
public abstract class LoadingOverlayMixin {
    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("TAIL"), remap = false)
    private void nows$renderLoadingDetails(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        NowsLoadingSnapshot loading = ClientHooks.loadingSnapshot();
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int width = Math.max(180, Math.min(360, screenWidth - 48));
        int x = (screenWidth - width) / 2;
        int y = Math.max(24, screenHeight - 58);

        graphics.drawString(minecraft.font, loading.title() + " - " + loading.stage(), x, y, loading.failed() ? 0xFFFF8080 : 0xFFFFFFFF, true);
        graphics.drawString(minecraft.font, loading.step() + " / " + loading.totalSteps(), x + width - 48, y, 0xFFE8E8E8, true);
        drawBar(graphics, x, y + 13, width, loading.progress(), loading.failed() ? 0xFFFF8080 : 0xFFFFFFFF);
        if (!loading.detail().isBlank()) {
            graphics.drawString(minecraft.font, loading.detail(), x, y + 19, 0xFFD8D8D8, true);
        }
        if (loading.subTotal() > 0) {
            graphics.drawString(minecraft.font, loading.subtask() + " " + loading.subStep() + " / " + loading.subTotal(), x, y + 31, 0xFFE8E8E8, true);
            drawBar(graphics, x, y + 44, width, loading.subProgress(), 0xFFE8E8E8);
        }
    }

    private static void drawBar(GuiGraphics graphics, int x, int y, int width, float progress, int color) {
        graphics.fill(x, y, x + width, y + 2, 0x55FFFFFF);
        graphics.fill(x, y, x + Math.max(1, Math.round(width * progress)), y + 2, color);
    }
}
