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

package space.nows.mcnows.mc.api.client.keybind;

import java.util.Objects;

/** Registered key mapping plus its stable Nows metadata. */
public record KeybindRegistration(
        String id,
        String category,
        int defaultKeyCode,
        Object nativeKeyMapping
) {
    public KeybindRegistration {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Keybind id must not be blank");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Keybind category must not be blank");
        }
        Objects.requireNonNull(nativeKeyMapping, "nativeKeyMapping");
    }

    public Object keyMapping() {
        return nativeKeyMapping;
    }

    public boolean consumeClick() {
        return invokeBoolean("consumeClick");
    }

    public boolean isDown() {
        return invokeBoolean("isDown");
    }

    private boolean invokeBoolean(String methodName) {
        try {
            return (Boolean) nativeKeyMapping.getClass().getMethod(methodName).invoke(nativeKeyMapping);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to call native key mapping method: " + methodName, exception);
        }
    }
}
