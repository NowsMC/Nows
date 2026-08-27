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

package space.nows.mc.internal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import space.nows.mc.api.client.ui.RenderContext;
import space.nows.mc.api.client.ui.ScreenInitializer;
import space.nows.mc.api.client.ui.ScreenRenderer;
import space.nows.mc.api.client.ui.TitleScreenUi;
import space.nows.mc.api.client.ui.Ui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class UiImpl implements Ui {
    public static final UiImpl INSTANCE = new UiImpl();

    private final TitleScreenUiImpl titleScreen = new TitleScreenUiImpl();
    private final List<ScreenRenderer> overlays = new CopyOnWriteArrayList<>();

    private UiImpl() {}

    @Override
    public TitleScreenUi titleScreen() {
        return titleScreen;
    }

    @Override
    public void showSimpleScreen(String title, ScreenInitializer initializer, ScreenRenderer renderer) {
        Minecraft.getInstance().setScreen(new SimpleScreen(Component.literal(title), initializer, renderer));
    }

    @Override
    public void closeScreen() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void overlay(ScreenRenderer renderer) {
        overlays.add(renderer);
    }

    @Override
    public int screenWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    @Override
    public int screenHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    public TitleScreenUiImpl titleScreenImpl() {
        return titleScreen;
    }

    public void renderOverlays(RenderContext context) {
        for (ScreenRenderer overlay : overlays) {
            overlay.render(context);
        }
    }
}
