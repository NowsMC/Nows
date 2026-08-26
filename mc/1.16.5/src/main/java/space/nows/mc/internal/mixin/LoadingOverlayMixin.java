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
import space.nows.mc.internal.ClientHooks;
import space.nows.platform.core.loading.NowsAsciiFont;
import space.nows.platform.core.loading.NowsLoadingDiagnostics;
import space.nows.platform.core.loading.NowsLoadingSnapshot;
import space.nows.platform.core.loading.NowsLoadingState;

@Mixin(value = LoadingOverlay.class, remap = false)
public abstract class LoadingOverlayMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", at = @At("TAIL"), remap = false)
    private void nows$renderLoaderDiagnostics(PoseStack graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        NowsLoadingSnapshot loading = NowsLoadingState.snapshot();
        NowsLoadingDiagnostics diagnostics = NowsLoadingDiagnostics.capture();
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        int statusColor = loading.failed() ? 0xFFFF8080 : 0xFFFFFFFF;
        drawLeft(graphics, ClientHooks.loaderLine() + " | " + ClientHooks.modLine(), 20, 20, width - 220, 0xCCFFFFFF);
        drawRight(graphics, "Minecraft " + ClientHooks.minecraftLine(), width - 20, 20, 180, 0xCCFFFFFF);
        drawRight(graphics, diagnostics.compactSummary(), width - 20, 34, Math.max(180, width - 40), 0x99FFFFFF);

        int panelWidth = Math.max(180, Math.min(460, width - 40));
        int panelX = (width - panelWidth) / 2;
        int panelY = Math.max(54, height - (loading.subTotal() > 0 ? 82 : 62));
        drawLabelValue(graphics, "Nows", loading.progressLabel(), panelX, panelY - 13, panelWidth, statusColor);
        drawFramedBar(graphics, panelX, panelY, panelWidth, 10, loading.overallProgress(), loading.failed() ? 0xFFFF4040 : 0xFF40BFFF);
        String detail = loading.currentDetailLine();
        if (!detail.isBlank()) {
            drawLeft(graphics, detail, panelX, panelY + 14, panelWidth, 0xDDFFFFFF);
        }
        if (loading.subTotal() > 0) {
            int subY = panelY + 29;
            drawLabelValue(graphics, loading.subtask().isBlank() ? "Work" : loading.subtask(),
                    loading.subProgressLabel(), panelX, subY, panelWidth, 0xDDFFFFFF);
            drawFramedBar(graphics, panelX, subY + 13, panelWidth, 8, loading.subProgress(), 0xFFA0E65C);
        }
    }

    private static void drawLabelValue(PoseStack graphics, String label, String value, int x, int y, int width, int color) {
        drawLeft(graphics, label, x, y, Math.max(24, width - 72), color);
        drawRight(graphics, value, x + width, y, 68, color);
    }

    private static void drawLeft(PoseStack graphics, String text, int x, int y, int maxWidth, int color) {
        drawAscii(graphics, fit(text, maxWidth), x, y, color);
    }

    private static void drawRight(PoseStack graphics, String text, int rightX, int y, int maxWidth, int color) {
        String fitted = fit(text, maxWidth);
        drawAscii(graphics, fitted, rightX - NowsAsciiFont.width(fitted, 1), y, color);
    }

    private static String fit(String text, int maxWidth) {
        if (NowsAsciiFont.width(text, 1) <= maxWidth) {
            return text;
        }
        String value = text;
        while (value.length() > 1 && NowsAsciiFont.width(value + "...", 1) > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "...";
    }

    private static void drawAscii(PoseStack graphics, String text, int x, int y, int color) {
        int cursor = x;
        for (int index = 0; index < text.length(); index++) {
            int[] glyph = NowsAsciiFont.glyph(text.charAt(index));
            for (int row = 0; row < NowsAsciiFont.GLYPH_HEIGHT; row++) {
                for (int column = 0; column < NowsAsciiFont.GLYPH_WIDTH; column++) {
                    if (((glyph[row] >> (NowsAsciiFont.GLYPH_WIDTH - column - 1)) & 1) != 0) {
                        GuiComponent.fill(graphics, cursor + column, y + row, cursor + column + 1, y + row + 1, color);
                    }
                }
            }
            cursor += NowsAsciiFont.GLYPH_WIDTH + NowsAsciiFont.GLYPH_SPACING;
        }
    }

    private static void drawFramedBar(PoseStack graphics, int x, int y, int width, int height, float progress, int color) {
        GuiComponent.fill(graphics, x, y, x + width, y + height, 0xFFFFFFFF);
        GuiComponent.fill(graphics, x + 2, y + 2, x + width - 2, y + height - 2, 0xAA000000);
        int fillWidth = Math.round((width - 6) * Math.max(0.0F, Math.min(1.0F, progress)));
        if (fillWidth > 0) {
            GuiComponent.fill(graphics, x + 3, y + 3, x + 3 + fillWidth, y + height - 3, color);
        }
    }
}
