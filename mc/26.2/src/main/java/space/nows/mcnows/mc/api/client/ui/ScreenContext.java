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

package space.nows.mcnows.mc.api.client.ui;

import space.nows.mcnows.api.NowsContext;

import java.util.function.Consumer;

public final class ScreenContext {
    private final int width;
    private final int height;
    private final ButtonSink buttons;
    private final SimpleScreenSink screens;
    private final Runnable close;
    private final Consumer<NowsContext> nowsMods;

    public ScreenContext(int width, int height, ButtonSink buttons, SimpleScreenSink screens, Runnable close) {
        this(width, height, buttons, screens, close, context -> close.run());
    }

    public ScreenContext(
            int width,
            int height,
            ButtonSink buttons,
            SimpleScreenSink screens,
            Runnable close,
            Consumer<NowsContext> nowsMods
    ) {
        this.width = width;
        this.height = height;
        this.buttons = buttons;
        this.screens = screens;
        this.close = close;
        this.nowsMods = nowsMods;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int centerX(int width) {
        return (width() - width) / 2;
    }

    public int centerY(int height) {
        return (height() - height) / 2;
    }

    public void addButton(int x, int y, int width, int height, String message, Runnable onPress) {
        buttons.addButton(x, y, width, height, message, onPress);
    }

    public void addIconButton(int x, int y, int width, int height, String icon, String message, Runnable onPress) {
        buttons.addIconButton(x, y, width, height, icon, message, onPress);
    }

    public void showSimpleScreen(String title, ScreenInitializer initializer, ScreenRenderer renderer) {
        screens.showSimpleScreen(title, initializer, renderer);
    }

    public void showNowsMods(NowsContext context) {
        nowsMods.accept(context);
    }

    public void close() {
        close.run();
    }
}
