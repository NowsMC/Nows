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

package space.nows.mc.internal.client.render.shader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

final class ShaderReflection {
    private ShaderReflection() {
    }

    static Optional<Class<?>> classForName(String name) {
        try {
            return Optional.of(Class.forName(name));
        } catch (ClassNotFoundException exception) {
            return Optional.empty();
        }
    }

    static Optional<Object> minecraft() {
        return classForName("net.minecraft.client.Minecraft")
                .flatMap(type -> tryCallStaticValue(type, "getInstance"));
    }

    static void call(Object target, String methodName) {
        call(target, methodName, new Class<?>[0]);
    }

    static void call(Object target, String methodName, Class<?>[] parameterTypes, Object... arguments) {
        if (!tryCall(target, methodName, parameterTypes, arguments)) {
            throw new IllegalStateException("Unable to call " + methodName + " on " + target);
        }
    }

    static boolean tryCall(Object target, String methodName) {
        return tryCall(target, methodName, new Class<?>[0]);
    }

    static boolean tryCall(Object target, String methodName, Class<?>[] parameterTypes, Object... arguments) {
        if (target == null) {
            return false;
        }
        try {
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            method.invoke(target, arguments);
            return true;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            return false;
        }
    }

    static void callStatic(Class<?> type, String methodName) {
        if (!tryCallStatic(type, methodName, new Class<?>[0])) {
            throw new IllegalStateException("Unable to call static " + methodName + " on " + type.getName());
        }
    }

    static boolean tryCallStatic(Class<?> type, String methodName, Class<?>[] parameterTypes, Object... arguments) {
        try {
            Method method = type.getMethod(methodName, parameterTypes);
            method.invoke(null, arguments);
            return true;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            return false;
        }
    }

    static Optional<Object> tryCallValue(Object target, String methodName) {
        return tryCallValue(target, methodName, new Class<?>[0]);
    }

    static Optional<Object> tryCallValue(
            Object target,
            String methodName,
            Class<?>[] parameterTypes,
            Object... arguments
    ) {
        if (target == null) {
            return Optional.empty();
        }
        try {
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            return Optional.ofNullable(method.invoke(target, arguments));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            return Optional.empty();
        }
    }

    static Optional<Object> tryCallStaticValue(
            Class<?> type,
            String methodName,
            Class<?>[] parameterTypes,
            Object... arguments
    ) {
        try {
            Method method = type.getMethod(methodName, parameterTypes);
            return Optional.ofNullable(method.invoke(null, arguments));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            return Optional.empty();
        }
    }

    static Optional<Object> tryCallStaticValue(Class<?> type, String methodName) {
        return tryCallStaticValue(type, methodName, new Class<?>[0]);
    }

    static Optional<Object> callFirstCompatible(Object target, String methodName, Object argument) {
        if (target == null || argument == null) {
            return Optional.empty();
        }
        for (Method method : target.getClass().getMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (method.getName().equals(methodName)
                    && parameterTypes.length == 1
                    && parameterTypes[0].isInstance(argument)) {
                try {
                    return Optional.ofNullable(method.invoke(target, argument));
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    static void callAssignable(Object target, String methodName, Object argument) {
        if (target == null || argument == null) {
            return;
        }
        for (Method method : target.getClass().getMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (method.getName().equals(methodName)
                    && parameterTypes.length == 1
                    && parameterTypes[0].isInstance(argument)) {
                try {
                    method.invoke(target, argument);
                    return;
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    throw new IllegalStateException("Unable to call " + methodName + " on " + target, exception);
                }
            }
        }
        throw new IllegalArgumentException("No compatible " + methodName + " method for " + argument.getClass().getName());
    }

    static Optional<Object> tryFieldValue(Object target, String fieldName) {
        if (target == null) {
            return Optional.empty();
        }
        try {
            Field field = target.getClass().getField(fieldName);
            return Optional.ofNullable(field.get(target));
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            return Optional.empty();
        }
    }

    static Optional<Object> tryStaticFieldValue(Class<?> type, String fieldName) {
        try {
            Field field = type.getField(fieldName);
            return Optional.ofNullable(field.get(null));
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            return Optional.empty();
        }
    }

    static int intField(Object target, String... names) {
        for (String name : names) {
            Optional<Object> value = tryFieldValue(target, name);
            if (value.isPresent() && value.get() instanceof Number number) {
                return number.intValue();
            }
        }
        return 0;
    }

    static int intCall(Object target, int fallback, String... names) {
        for (String name : names) {
            Optional<Object> value = tryCallValue(target, name);
            if (value.isPresent() && value.get() instanceof Number number) {
                return number.intValue();
            }
        }
        return fallback;
    }

    static int staticIntCall(
            Class<?> type,
            int fallback,
            String methodName,
            Class<?>[] parameterTypes,
            Object... arguments
    ) {
        Optional<Object> value = tryCallStaticValue(type, methodName, parameterTypes, arguments);
        return value.filter(Number.class::isInstance).map(Number.class::cast).map(Number::intValue).orElse(fallback);
    }

    static boolean staticBooleanCall(Class<?> type, boolean fallback, String methodName) {
        Optional<Object> value = tryCallStaticValue(type, methodName);
        return value.filter(Boolean.class::isInstance).map(Boolean.class::cast).orElse(fallback);
    }

    static int staticIntField(Class<?> type, int fallback, String name) {
        Optional<Object> value = tryStaticFieldValue(type, name);
        return value.filter(Number.class::isInstance).map(Number.class::cast).map(Number::intValue).orElse(fallback);
    }

    static boolean staticBooleanField(Class<?> type, boolean fallback, String... names) {
        for (String name : names) {
            Optional<Object> value = tryStaticFieldValue(type, name);
            if (value.isPresent() && value.get() instanceof Boolean bool) {
                return bool;
            }
        }
        return fallback;
    }
}
