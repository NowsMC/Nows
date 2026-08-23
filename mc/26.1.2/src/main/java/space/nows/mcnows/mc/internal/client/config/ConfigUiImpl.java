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

package space.nows.mcnows.mc.internal.client.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import space.nows.mcnows.mc.api.client.config.ConfigScreenBuilder;
import space.nows.mcnows.mc.api.client.config.ConfigScreenFactory;
import space.nows.mcnows.mc.api.client.config.ConfigUi;
import space.nows.mcnows.mc.api.text.McText;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigUiImpl implements ConfigUi {
    private final Map<String, ConfigScreenFactory> factories = new ConcurrentHashMap<>();

    @Override
    public ConfigScreenBuilder screen(Screen parent, Component title) {
        return new ConfigScreenBuilder(parent, title, spec -> new SimpleConfigScreen(spec.parent(), spec));
    }


    public ConfigScreenBuilder screen(Screen parent, McText title) {
        return screen(parent, component(title));
    }


    public ConfigScreenBuilder screen(McText title) {
        return screen(null, title);
    }

    private static Component component(McText text) {
        if (text == null) {
            return Component.literal("");
        }
        return switch (text.type()) {
            case LITERAL -> Component.literal(text.value());
            case TRANSLATABLE -> Component.translatable(text.value(), text.args());
            case KEYBIND -> Component.keybind(text.value());
        };
    }

    @Override
    public void register(String modId, ConfigScreenFactory factory) {
        factories.put(modId, factory);
    }

    @Override
    public Optional<Screen> create(String modId, Screen parent) {
        ConfigScreenFactory factory = factories.get(modId);
        return factory == null ? Optional.empty() : Optional.ofNullable(factory.create(parent));
    }

    @Override
    public boolean has(String modId) {
        return factories.containsKey(modId);
    }
}
