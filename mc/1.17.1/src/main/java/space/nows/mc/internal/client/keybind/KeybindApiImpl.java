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

package space.nows.mc.internal.client.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import space.nows.mc.api.client.keybind.KeybindApi;
import space.nows.mc.api.client.keybind.KeybindRegistration;
import space.nows.mc.internal.event.GameEventsImpl;
import space.nows.mc.internal.mixin.KeyMappingAccessor;
import space.nows.mc.internal.mixin.OptionsAccessor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class KeybindApiImpl implements KeybindApi {
    private final Map<String, KeybindRegistration> keybinds = new LinkedHashMap<>();
    private final Map<String, Integer> categories = new LinkedHashMap<>();
    private final Map<String, List<Runnable>> pressListeners = new LinkedHashMap<>();

    public KeybindApiImpl() {
        registerCategory(DEFAULT_CATEGORY);
        GameEventsImpl.INSTANCE.clientTick(ignored -> dispatchPresses());
    }

    @Override
    public synchronized void registerCategory(String category) {
        registerCategory(category, 1000 + categories.size());
    }

    @Override
    public synchronized void registerCategory(String category, int sortOrder) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Keybind category must not be blank");
        }
        categories.putIfAbsent(category, sortOrder);
        registerMinecraftCategory(category, sortOrder);
    }

    @Override
    public KeybindRegistration registerKeyboard(String id, int glfwKeyCode) {
        return registerKeyboard(id, DEFAULT_CATEGORY, glfwKeyCode);
    }

    @Override
    public KeybindRegistration registerKeyboard(String id, String category, int glfwKeyCode) {
        return registerKeyboard(id, category, glfwKeyCode, null);
    }

    @Override
    public synchronized KeybindRegistration registerKeyboard(String id, String category, int glfwKeyCode, Runnable onPress) {
        registerCategory(category);
        if (keybinds.containsKey(id)) {
            throw new IllegalStateException("Keybind already registered: " + id);
        }
        KeyMapping mapping = createKeyboardMapping(id, category, glfwKeyCode);
        KeybindRegistration registration = new KeybindRegistration(id, category, glfwKeyCode, mapping);
        keybinds.put(id, registration);
        appendToMinecraftOptions(mapping);
        if (onPress != null) {
            onPress(id, onPress);
        }
        return registration;
    }

    @Override
    public synchronized void onPress(String id, Runnable listener) {
        if (!keybinds.containsKey(id)) {
            throw new IllegalArgumentException("Keybind is not registered: " + id);
        }
        pressListeners.computeIfAbsent(id, ignored -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @Override
    public synchronized Optional<KeybindRegistration> keybind(String id) {
        return Optional.ofNullable(keybinds.get(id));
    }

    @Override
    public synchronized List<KeybindRegistration> keybinds() {
        return List.copyOf(keybinds.values());
    }

    private void dispatchPresses() {
        List<KeybindRegistration> snapshot;
        synchronized (this) {
            snapshot = List.copyOf(keybinds.values());
        }
        for (KeybindRegistration registration : snapshot) {
            while (registration.consumeClick()) {
                runPressListeners(registration.id());
            }
        }
    }

    private void runPressListeners(String id) {
        List<Runnable> listeners;
        synchronized (this) {
            listeners = List.copyOf(pressListeners.getOrDefault(id, List.of()));
        }
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    static KeyMapping createKeyboardMapping(String id, String category, int glfwKeyCode) {
        return new KeyMapping(id, InputConstants.Type.KEYSYM, glfwKeyCode, category);
    }

    private static void appendToMinecraftOptions(KeyMapping mapping) {
        Options options = Minecraft.getInstance().options;
        if (options == null) {
            return;
        }
        appendMapping((OptionsAccessor) options, mapping);
        KeyMapping.resetMapping();
    }

    private static void appendMapping(OptionsAccessor options, KeyMapping mapping) {
        KeyMapping[] current = options.nows$getKeyMappings();
        for (KeyMapping existing : current) {
            if (existing == mapping || existing.getName().equals(mapping.getName())) {
                return;
            }
        }
        KeyMapping[] next = new KeyMapping[current.length + 1];
        System.arraycopy(current, 0, next, 0, current.length);
        next[current.length] = mapping;
        options.nows$setKeyMappings(next);
    }

    private static void registerMinecraftCategory(String category, int sortOrder) {
        KeyMappingAccessor.nows$getCategorySortOrder().putIfAbsent(category, sortOrder);
    }
}
