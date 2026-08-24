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
import space.nows.mc.internal.ClientHooks;
import space.nows.platform.core.loading.NowsLoadingDiagnostics;
import space.nows.platform.core.loading.NowsLoadingSnapshot;
import space.nows.platform.core.loading.NowsLoadingState;

@Mixin(value = LoadingOverlay.class, remap = false)
public abstract class LoadingOverlayMixin {
    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("TAIL"), remap = false)
    private void nows$renderLoaderDiagnostics(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        NowsLoadingSnapshot loading = NowsLoadingState.snapshot();
        NowsLoadingDiagnostics diagnostics = NowsLoadingDiagnostics.capture();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        int topBarWidth = Math.max(140, Math.min(460, width - 120));
        int topX = (width - topBarWidth) / 2;
        drawFramedBar(graphics, topX, 24, topBarWidth, 9, diagnostics.heapProgress(), 0xFF00A000);
        drawCentered(graphics, minecraft, diagnostics.summary(), width / 2, 45, width - 40, 0xFFFFFFFF);

        String stage = loading.stage();
        if (!loading.detail().isBlank() && !"Done".equals(loading.detail())) {
            stage += ": " + loading.detail();
        }
        int centerY = Math.max(86, height - 190);
        drawCentered(graphics, minecraft, stage, width / 2, centerY, width - 80,
                loading.failed() ? 0xFFFF8080 : 0xFFFFFFFF);
        if (loading.subTotal() > 0) {
            drawCentered(graphics, minecraft,
                    loading.subtask() + " " + loading.subStep() + "/" + loading.subTotal(),
                    width / 2, centerY + 12, width - 80, 0xFFFFFFFF);
        }

        int y = Math.max(20, height - 64);
        drawLeft(graphics, minecraft, ClientHooks.loaderLine(), 20, y, width - 160, 0x88FFFFFF);
        drawLeft(graphics, minecraft, "Nows setup: " + loading.stage(), 20, y + 14, width - 160,
                loading.failed() ? 0xFFFF8080 : 0xFFFFFFFF);
        if (!loading.history().isEmpty()) {
            drawLeft(graphics, minecraft, "Finished: " + loading.history().get(0), 20, y + 28, width - 160, 0xFFFFFFFF);
        }
        String version = ClientHooks.minecraftLine();
        graphics.drawString(minecraft.font, version, width - minecraft.font.width(version) - 20, height - 24, 0xFFFFFFFF, true);
    }

    private static void drawCentered(GuiGraphics graphics, Minecraft minecraft, String text, int centerX, int y, int maxWidth, int color) {
        String fitted = fit(minecraft, text, maxWidth);
        graphics.drawString(minecraft.font, fitted, centerX - minecraft.font.width(fitted) / 2, y, color, true);
    }

    private static void drawLeft(GuiGraphics graphics, Minecraft minecraft, String text, int x, int y, int maxWidth, int color) {
        graphics.drawString(minecraft.font, fit(minecraft, text, maxWidth), x, y, color, true);
    }

    private static String fit(Minecraft minecraft, String text, int maxWidth) {
        if (minecraft.font.width(text) <= maxWidth) {
            return text;
        }
        String value = text;
        while (value.length() > 1 && minecraft.font.width(value + "...") > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "...";
    }

    private static void drawFramedBar(GuiGraphics graphics, int x, int y, int width, int height, float progress, int color) {
        graphics.fill(x, y, x + width, y + height, 0xFFFFFFFF);
        int fillWidth = Math.round((width - 6) * Math.max(0.0F, Math.min(1.0F, progress)));
        if (fillWidth > 0) {
            graphics.fill(x + 3, y + 3, x + 3 + fillWidth, y + height - 3, color);
        }
    }
}
