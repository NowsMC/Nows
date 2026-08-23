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

package space.nows.mcnows.mc.internal.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import space.nows.mcnows.mc.api.text.TextApi;

public enum TextApiImpl implements TextApi {
    INSTANCE;

    @Override
    public Component literal(String text) {
        return new TextComponent(text);
    }

    @Override
    public Component translatable(String key, Object... args) {
        return new TranslatableComponent(key, args);
    }

    @Override
    public Component keybind(String key) {
        return new KeybindComponent(key);
    }
}
