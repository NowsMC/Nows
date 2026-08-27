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
import net.minecraft.Util;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
    @Shadow
    @Final
    private boolean fadeIn;

    @Shadow
    private long fadeOutStart;

    @Shadow
    private long fadeInStart;

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("TAIL"), remap = false)
    private void nows$renderLoaderDiagnostics(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        NowsLoadingSnapshot loading = NowsLoadingState.snapshot();
        NowsLoadingDiagnostics diagnostics = NowsLoadingDiagnostics.capture();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        float alpha = nows$loadingAlpha();
        if (alpha <= 0.0F) {
            return;
        }

        int statusColor = fadeColor(loading.failed() ? 0xFFFF8080 : 0xFFFFFFFF, alpha);
        drawLeft(graphics, ClientHooks.loaderLine() + " | " + ClientHooks.modLine(), 20, 20, width - 220, fadeColor(0xCCFFFFFF, alpha));
        drawRight(graphics, "Minecraft " + ClientHooks.minecraftLine(), width - 20, 20, 180, fadeColor(0xCCFFFFFF, alpha));
        drawRight(graphics, diagnostics.compactSummary(), width - 20, 34, Math.max(180, width - 40), fadeColor(0x99FFFFFF, alpha));

        int panelWidth = Math.max(180, Math.min(480, width - 40));
        int panelX = (width - panelWidth) / 2;
        int panelY = Math.max(54, height - (height < 540 ? (loading.subTotal() > 0 ? 66 : 46) : (loading.subTotal() > 0 ? 88 : 68)));
        drawLabelValue(graphics, "Nows", loading.displayProgressLabel(), panelX, panelY - 13, panelWidth, statusColor);
        drawStatusStrip(graphics, panelX, panelY, panelWidth, loading.displayProgress(),
                fadeColor(loading.failed() ? 0xFFFF4040 : 0xFF40BFFF, alpha));
        String detail = loading.currentDetailLine();
        if (!detail.isBlank()) {
            drawLeft(graphics, detail, panelX, panelY + 7, panelWidth, fadeColor(0xDDFFFFFF, alpha));
        }
        if (loading.subTotal() > 0) {
            int subY = panelY + 22;
            drawLabelValue(graphics, loading.subtask().isBlank() ? "Work" : loading.subtask(),
                    loading.subProgressLabel(), panelX, subY, panelWidth, fadeColor(0xDDFFFFFF, alpha));
            drawStatusStrip(graphics, panelX, subY + 13, panelWidth, loading.subProgress(), fadeColor(0xFFA0E65C, alpha));
        }
    }

    private static void drawLabelValue(GuiGraphics graphics, String label, String value, int x, int y, int width, int color) {
        drawLeft(graphics, label, x, y, Math.max(24, width - 72), color);
        drawRight(graphics, value, x + width, y, 68, color);
    }

    private static void drawLeft(GuiGraphics graphics, String text, int x, int y, int maxWidth, int color) {
        drawAscii(graphics, fit(text, maxWidth), x, y, color);
    }

    private static void drawRight(GuiGraphics graphics, String text, int rightX, int y, int maxWidth, int color) {
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

    private static void drawAscii(GuiGraphics graphics, String text, int x, int y, int color) {
        int cursor = x;
        for (int index = 0; index < text.length(); index++) {
            int[] glyph = NowsAsciiFont.glyph(text.charAt(index));
            for (int row = 0; row < NowsAsciiFont.GLYPH_HEIGHT; row++) {
                for (int column = 0; column < NowsAsciiFont.GLYPH_WIDTH; column++) {
                    if (((glyph[row] >> (NowsAsciiFont.GLYPH_WIDTH - column - 1)) & 1) != 0) {
                        graphics.fill(cursor + column, y + row, cursor + column + 1, y + row + 1, color);
                    }
                }
            }
            cursor += NowsAsciiFont.GLYPH_WIDTH + NowsAsciiFont.GLYPH_SPACING;
        }
    }

    private static void drawStatusStrip(GuiGraphics graphics, int x, int y, int width, float progress, int color) {
        graphics.fill(x, y, x + width, y + 2, color & 0x44FFFFFF);
        int fillWidth = Math.round(width * Math.max(0.0F, Math.min(1.0F, progress)));
        if (fillWidth > 0) {
            graphics.fill(x, y, x + fillWidth, y + 2, color);
        }
    }

    private float nows$loadingAlpha() {
        long now = Util.getMillis();
        float alpha = 1.0F;
        if (fadeOutStart > -1L) {
            alpha = 1.0F - Mth.clamp((float) (now - fadeOutStart) / (float) LoadingOverlay.FADE_OUT_TIME, 0.0F, 1.0F);
        }
        if (fadeIn && fadeInStart > -1L) {
            alpha = Math.min(alpha, Mth.clamp((float) (now - fadeInStart) / (float) LoadingOverlay.FADE_IN_TIME, 0.0F, 1.0F));
        }
        return alpha;
    }

    private static int fadeColor(int color, float alpha) {
        int baseAlpha = color >>> 24;
        int fadedAlpha = Mth.clamp(Math.round(baseAlpha * alpha), 0, 255);
        return (color & 0x00FFFFFF) | (fadedAlpha << 24);
    }
}
