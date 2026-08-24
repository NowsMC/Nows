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
        int width = Math.min(460, Math.max(280, screenWidth - 48));
        int x = (screenWidth - width) / 2;
        int y = Math.max(24, screenHeight - 118);
        int progressWidth = Math.max(1, Math.round((width - 24) * loading.progress()));

        graphics.fill(x, y, x + width, y + 92, 0xAA05070C);
        graphics.fill(x, y, x + width, y + 1, 0xFF6D8CFF);
        graphics.drawString(minecraft.font, loading.title(), x + 12, y + 10, 0xFFFFFFFF, true);
        graphics.drawString(minecraft.font, loading.stage(), x + 12, y + 25, loading.failed() ? 0xFFFF7070 : 0xFFE8ECFF, true);
        if (!loading.detail().isBlank()) {
            graphics.drawString(minecraft.font, loading.detail(), x + 12, y + 40, 0xFFB8C0D8, true);
        }
        graphics.fill(x + 12, y + 58, x + width - 12, y + 64, 0xFF242938);
        graphics.fill(x + 12, y + 58, x + 12 + progressWidth, y + 64, 0xFF6D8CFF);
        graphics.drawString(minecraft.font, loading.step() + " / " + loading.totalSteps(), x + width - 58, y + 69, 0xFFB8C0D8, true);
        int lineY = y + 69;
        for (String line : loading.history()) {
            graphics.drawString(minecraft.font, line, x + 12, lineY, 0xFF8F98B4, true);
            lineY += 10;
            if (lineY > y + 86) {
                break;
            }
        }
    }
}
