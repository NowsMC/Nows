package space.nows.mcnows.mc.internal.client.keybind;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import space.nows.mcnows.mc.api.client.keybind.KeybindApi;
import space.nows.mcnows.mc.api.client.keybind.KeybindRegistration;
import space.nows.mcnows.mc.internal.event.GameEventsImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static KeyMapping createKeyboardMapping(String id, String category, int glfwKeyCode) {
        try {
            Class<?> typeClass = Class.forName("com.mojang.blaze3d.platform.InputConstants$Type");
            Object keysym = Enum.valueOf((Class<Enum>) typeClass.asSubclass(Enum.class), "KEYSYM");
            Constructor<KeyMapping> constructor = KeyMapping.class
                    .getConstructor(String.class, typeClass, int.class, String.class);
            return constructor.newInstance(id, keysym, glfwKeyCode, category);
        } catch (ReflectiveOperationException ignored) {
            try {
                Constructor<KeyMapping> constructor = KeyMapping.class
                        .getConstructor(String.class, int.class, String.class);
                return constructor.newInstance(id, glfwKeyCode, category);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unsupported KeyMapping constructor", exception);
            }
        }
    }

    private static void appendToMinecraftOptions(KeyMapping mapping) {
        Object options = Minecraft.getInstance().options;
        if (options == null) {
            return;
        }
        for (Field field : options.getClass().getDeclaredFields()) {
            if (field.getType().isArray() && field.getType().getComponentType() == KeyMapping.class) {
                appendMapping(options, field, mapping);
                resetMapping();
                return;
            }
        }
    }

    private static void appendMapping(Object options, Field field, KeyMapping mapping) {
        try {
            field.setAccessible(true);
            KeyMapping[] current = (KeyMapping[]) field.get(options);
            for (KeyMapping existing : current) {
                if (existing == mapping || existing.getName().equals(mapping.getName())) {
                    return;
                }
            }
            KeyMapping[] next = new KeyMapping[current.length + 1];
            System.arraycopy(current, 0, next, 0, current.length);
            next[current.length] = mapping;
            field.set(options, next);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to register keybind with Minecraft options", exception);
        }
    }

    private static void registerMinecraftCategory(String category, int sortOrder) {
        for (Field field : KeyMapping.class.getDeclaredFields()) {
            if (!Map.class.isAssignableFrom(field.getType()) || !Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(null);
                if (value instanceof Map<?, ?> map && isCategorySortMap(map)) {
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> categories = (Map<String, Integer>) map;
                    categories.putIfAbsent(category, sortOrder);
                    return;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }

    private static boolean isCategorySortMap(Map<?, ?> map) {
        return map.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof String && entry.getValue() instanceof Integer)
                .map(entry -> (String) entry.getKey())
                .anyMatch(key -> key.startsWith("key.categories."));
    }

    private static void resetMapping() {
        try {
            KeyMapping.class.getMethod("resetMapping").invoke(null);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
