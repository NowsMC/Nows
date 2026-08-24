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
        int width = Math.min(460, Math.max(280, screenWidth - 48));
        int x = (screenWidth - width) / 2;
        int y = Math.max(24, screenHeight - 118);
        int progressWidth = Math.max(1, Math.round((width - 24) * loading.progress()));

        GuiComponent.fill(graphics, x, y, x + width, y + 92, 0xAA05070C);
        GuiComponent.fill(graphics, x, y, x + width, y + 1, 0xFF6D8CFF);
        GuiComponent.drawString(graphics, minecraft.font, loading.title(), x + 12, y + 10, 0xFFFFFFFF);
        GuiComponent.drawString(graphics, minecraft.font, loading.stage(), x + 12, y + 25, loading.failed() ? 0xFFFF7070 : 0xFFE8ECFF);
        if (!loading.detail().isBlank()) {
            GuiComponent.drawString(graphics, minecraft.font, loading.detail(), x + 12, y + 40, 0xFFB8C0D8);
        }
        GuiComponent.fill(graphics, x + 12, y + 58, x + width - 12, y + 64, 0xFF242938);
        GuiComponent.fill(graphics, x + 12, y + 58, x + 12 + progressWidth, y + 64, 0xFF6D8CFF);
        GuiComponent.drawString(graphics, minecraft.font, loading.step() + " / " + loading.totalSteps(), x + width - 58, y + 69, 0xFFB8C0D8);
        int lineY = y + 69;
        for (String line : loading.history()) {
            GuiComponent.drawString(graphics, minecraft.font, line, x + 12, lineY, 0xFF8F98B4);
            lineY += 10;
            if (lineY > y + 86) {
                break;
            }
        }
    }
}
