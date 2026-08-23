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

package space.nows.mc.api.client.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import space.nows.mc.api.text.McText;

import java.util.Optional;

public interface ConfigUi {
    ConfigScreenBuilder screen(Screen parent, Component title);

    ConfigScreenBuilder screen(Screen parent, McText title);

    ConfigScreenBuilder screen(McText title);

    default ConfigScreenBuilder screen(String title) {
        return screen(McText.literal(title));
    }

    default ConfigScreenBuilder screen(Screen parent, String title) {
        return screen(parent, McText.literal(title));
    }


    void register(String modId, ConfigScreenFactory factory);

    Optional<Screen> create(String modId, Screen parent);

    boolean has(String modId);
}
