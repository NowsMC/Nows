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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Converts stable Nows text values to the selected Minecraft version's native
 * text component type without baking one Minecraft text API into shared code.
 */
public final class NativeTextBridge {
    private static final String COMPONENT_CLASS = "net.minecraft.network.chat.Component";
    private static final String TEXT_COMPONENT_CLASS = "net.minecraft.network.chat.TextComponent";
    private static final String TRANSLATABLE_COMPONENT_CLASS = "net.minecraft.network.chat.TranslatableComponent";
    private static final String KEYBIND_COMPONENT_CLASS = "net.minecraft.network.chat.KeybindComponent";

    private NativeTextBridge() {
    }

    public static <T> T nativeComponent(McText text, Class<T> componentType) {
        Object component = component(text);
        return componentType.cast(component);
    }

    private static Object component(McText text) {
        if (text == null) {
            return literal("");
        }
        return switch (text.type()) {
            case LITERAL -> literal(text.value());
            case TRANSLATABLE -> translatable(text.value(), text.args());
            case KEYBIND -> keybind(text.value());
        };
    }

    private static Object literal(String text) {
        Object component = invokeStatic("literal", new Class<?>[] { String.class }, text);
        return component == null ? construct(TEXT_COMPONENT_CLASS, new Class<?>[] { String.class }, text) : component;
    }

    private static Object translatable(String key, Object[] args) {
        Object component = invokeStatic("translatable", new Class<?>[] { String.class, Object[].class }, key, args);
        return component == null
                ? construct(TRANSLATABLE_COMPONENT_CLASS, new Class<?>[] { String.class, Object[].class }, key, args)
                : component;
    }

    private static Object keybind(String key) {
        Object component = invokeStatic("keybind", new Class<?>[] { String.class }, key);
        return component == null ? construct(KEYBIND_COMPONENT_CLASS, new Class<?>[] { String.class }, key) : component;
    }

    private static Object invokeStatic(String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = Class.forName(COMPONENT_CLASS).getMethod(methodName, parameterTypes);
            return method.invoke(null, args);
        }
        catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    private static Object construct(String className, Class<?>[] parameterTypes, Object... args) {
        try {
            Constructor<?> constructor = Class.forName(className).getConstructor(parameterTypes);
            return constructor.newInstance(args);
        }
        catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to create Minecraft text component: " + className, exception);
        }
    }
}
