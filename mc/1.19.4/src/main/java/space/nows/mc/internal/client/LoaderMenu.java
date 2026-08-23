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

import space.nows.platform.api.NowsContext;
import space.nows.mc.api.MinecraftApi;
import space.nows.mc.api.client.ui.Ui;

public final class LoaderMenu {
    private static final String ICON = "nows:textures/gui/mod_menu_icon.png";

    private LoaderMenu() {
    }

    public static void install(NowsContext context) {
        Ui ui = MinecraftApi.ui(context);
        ui.titleScreen().addButton(title -> title.addIconButton(
                title.width() - 24, 4, 20, 20,
                ICON,
                "Nows Mods",
                () -> title.showNowsMods(context)
        ));
    }
}
