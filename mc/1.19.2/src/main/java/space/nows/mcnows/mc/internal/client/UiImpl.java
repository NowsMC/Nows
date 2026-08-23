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

package space.nows.mcnows.mc.internal.client;

import space.nows.mcnows.mc.api.client.ui.TitleScreenUi;
import space.nows.mcnows.mc.api.client.ui.Ui;

public final class UiImpl implements Ui {
    public static final UiImpl INSTANCE = new UiImpl();

    private final TitleScreenUiImpl titleScreen = new TitleScreenUiImpl();

    private UiImpl() {}

    @Override
    public TitleScreenUi titleScreen() {
        return titleScreen;
    }

    public TitleScreenUiImpl titleScreenImpl() {
        return titleScreen;
    }
}
