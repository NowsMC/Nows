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

public final class RenderContext {
    private final int width;
    private final int height;
    private final int mouseX;
    private final int mouseY;
    private final float delta;
    private final RenderSink renderer;

    public RenderContext(int width, int height, int mouseX, int mouseY, float delta, RenderSink renderer) {
        this.width = width;
        this.height = height;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.delta = delta;
        this.renderer = renderer;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int mouseX() {
        return mouseX;
    }

    public int mouseY() {
        return mouseY;
    }

    public float delta() {
        return delta;
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        renderer.fill(x1, y1, x2, y2, color);
    }

    public void text(String text, int x, int y, int color) {
        renderer.text(text, x, y, color);
    }

    public void centeredText(String text, int x, int y, int color) {
        renderer.centeredText(text, x, y, color);
    }

    public void icon(String id, int x, int y, int width, int height) {
        renderer.icon(id, x, y, width, height);
    }
}
