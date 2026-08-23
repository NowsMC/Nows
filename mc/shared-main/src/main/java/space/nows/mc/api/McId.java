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

package space.nows.mc.api;

import java.util.Objects;

/** Stable namespaced identifier independent of Minecraft's Identifier/ResourceLocation classes. */
public record McId(String namespace, String path) {
    public McId {
        namespace = requirePart(namespace, "namespace");
        path = requirePart(path, "path");
    }

    public static McId of(String id) {
        Objects.requireNonNull(id, "id");
        int separator = id.indexOf(':');
        if (separator < 0) {
            return new McId("minecraft", id);
        }
        return new McId(id.substring(0, separator), id.substring(separator + 1));
    }

    public static McId of(String namespace, String path) {
        return new McId(namespace, path);
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }

    private static String requirePart(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
