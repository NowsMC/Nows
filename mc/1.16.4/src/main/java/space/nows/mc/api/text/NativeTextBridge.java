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

package space.nows.mc.api.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

/** Converts stable Nows text values to this adapter's native component type. */
public final class NativeTextBridge {
    private NativeTextBridge() {
    }

    public static <T> T nativeComponent(McText text, Class<T> componentType) {
        return componentType.cast(component(text));
    }

    private static Component component(McText text) {
        if (text == null) {
            return literal("");
        }
        return switch (text.type()) {
            case LITERAL -> literal(text.value());
            case TRANSLATABLE -> translatable(text.value(), text.args());
            case KEYBIND -> keybind(text.value());
        };
    }

    private static Component literal(String text) {
        return new TextComponent(text);
    }

    private static Component translatable(String key, Object[] args) {
        return new TranslatableComponent(key, args);
    }

    private static Component keybind(String key) {
        return new KeybindComponent(key);
    }
}
