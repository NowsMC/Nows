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

import space.nows.mc.api.client.ui.RenderContext;
import space.nows.mc.api.client.ui.ScreenContext;
import space.nows.mc.api.client.ui.ScreenRenderer;
import space.nows.mc.api.client.ui.TitleButtonFactory;
import space.nows.mc.api.client.ui.TitleScreenUi;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class TitleScreenUiImpl implements TitleScreenUi {
    private final List<TitleButtonFactory> buttonFactories = new CopyOnWriteArrayList<>();
    private final List<ScreenRenderer> renderers = new CopyOnWriteArrayList<>();

    @Override
    public void addButton(TitleButtonFactory factory) {
        buttonFactories.add(factory);
    }

    @Override
    public void render(ScreenRenderer renderer) {
        renderers.add(renderer);
    }

    public void addButtons(ScreenContext context) {
        for (TitleButtonFactory factory : buttonFactories) {
            factory.add(context);
        }
    }

    public void renderAll(RenderContext context) {
        for (ScreenRenderer renderer : renderers) {
            renderer.render(context);
        }
    }
}
