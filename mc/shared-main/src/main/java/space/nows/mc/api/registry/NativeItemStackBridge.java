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

package space.nows.mc.api.registry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Converts stable Nows item stack values to native Minecraft ItemStack
 * instances for APIs that still expose native viewer or inventory types.
 */
public final class NativeItemStackBridge {
    private static final String ITEM_STACK_CLASS = "net.minecraft.world.item.ItemStack";
    private static final String ITEM_CLASS = "net.minecraft.world.item.Item";
    private static final String BUILT_IN_REGISTRIES_CLASS = "net.minecraft.core.registries.BuiltInRegistries";
    private static final String REGISTRY_CLASS = "net.minecraft.core.Registry";

    private NativeItemStackBridge() {
    }

    public static <T> T nativeStack(McItemStack stack, Class<T> stackType) {
        return nativeStack(stack.toSpec(), stackType);
    }

    public static <T> T nativeStack(ItemStackSpec spec, Class<T> stackType) {
        try {
            Object item = item(spec.itemId());
            Constructor<?> constructor = Class.forName(ITEM_STACK_CLASS).getConstructor(Class.forName(ITEM_CLASS), int.class);
            return stackType.cast(constructor.newInstance(item, spec.count()));
        }
        catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to create Minecraft item stack: " + spec.itemId(), exception);
        }
    }

    private static Object item(String itemId) throws ReflectiveOperationException {
        Object registry = itemRegistry();
        Object id = resourceId(itemId);
        Method getOptional = registry.getClass().getMethod("getOptional", id.getClass());
        Optional<?> item = (Optional<?>) getOptional.invoke(registry, id);
        return item.orElseThrow(() -> new IllegalArgumentException("Unknown Minecraft item: " + itemId));
    }

    private static Object itemRegistry() throws ReflectiveOperationException {
        Object registry = staticField(BUILT_IN_REGISTRIES_CLASS, "ITEM");
        return registry == null ? staticField(REGISTRY_CLASS, "ITEM") : registry;
    }

    private static Object staticField(String className, String fieldName) throws ReflectiveOperationException {
        try {
            Field field = Class.forName(className).getField(fieldName);
            return field.get(null);
        }
        catch (ClassNotFoundException exception) {
            return null;
        }
    }

    private static Object resourceId(String id) throws ReflectiveOperationException {
        Object identifier = identifier("net.minecraft.resources.Identifier", id);
        return identifier == null ? identifier("net.minecraft.resources.ResourceLocation", id) : identifier;
    }

    private static Object identifier(String className, String id) throws ReflectiveOperationException {
        try {
            Class<?> type = Class.forName(className);
            Object value = invokeStatic(type, "parse", id);
            if (value != null) {
                return value;
            }
            value = invokeStatic(type, "tryParse", id);
            if (value != null) {
                return value;
            }
            return type.getConstructor(String.class).newInstance(id);
        }
        catch (ClassNotFoundException exception) {
            return null;
        }
    }

    private static Object invokeStatic(Class<?> type, String methodName, String id) {
        try {
            Method method = type.getMethod(methodName, String.class);
            return method.invoke(null, id);
        }
        catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}
