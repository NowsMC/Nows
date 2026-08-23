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

package space.nows.mcnows.mc.api.text;

import java.util.Arrays;

/** Stable text description translated to Minecraft Component by each adapter. */
public final class McText {
    public enum Type {
        LITERAL,
        TRANSLATABLE,
        KEYBIND
    }

    private final Type type;
    private final String value;
    private final Object[] args;

    private McText(Type type, String value, Object[] args) {
        this.type = type;
        this.value = value == null ? "" : value;
        this.args = args == null ? new Object[0] : Arrays.copyOf(args, args.length);
    }

    public static McText literal(String text) {
        return new McText(Type.LITERAL, text, null);
    }

    public static McText translatable(String key, Object... args) {
        return new McText(Type.TRANSLATABLE, key, args);
    }

    public static McText keybind(String key) {
        return new McText(Type.KEYBIND, key, null);
    }

    public Type type() {
        return type;
    }

    public String value() {
        return value;
    }

    public Object[] args() {
        return Arrays.copyOf(args, args.length);
    }
}
