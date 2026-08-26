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
        namespace = requireNamespace(namespace);
        path = requirePath(path);
    }

    public static McId of(String id) {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        int separator = id.indexOf(':');
        if (separator != id.lastIndexOf(':')) {
            throw new IllegalArgumentException("id must contain at most one namespace separator: " + id);
        }
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

    public static String requireId(String id) {
        McId.of(id);
        return id;
    }

    private static String requireNamespace(String value) {
        String namespace = requirePart(value, "namespace");
        for (int i = 0; i < namespace.length(); i++) {
            char character = namespace.charAt(i);
            if (!isNamespaceCharacter(character)) {
                throw new IllegalArgumentException("namespace contains invalid character '" + character + "': " + namespace);
            }
        }
        return namespace;
    }

    private static String requirePath(String value) {
        String path = requirePart(value, "path");
        for (int i = 0; i < path.length(); i++) {
            char character = path.charAt(i);
            if (!isPathCharacter(character)) {
                throw new IllegalArgumentException("path contains invalid character '" + character + "': " + path);
            }
        }
        return path;
    }

    private static String requirePart(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        if (!value.equals(value.trim())) {
            throw new IllegalArgumentException(name + " must not have leading or trailing whitespace");
        }
        return value;
    }

    private static boolean isNamespaceCharacter(char character) {
        return character >= 'a' && character <= 'z'
                || character >= '0' && character <= '9'
                || character == '_' || character == '-' || character == '.';
    }

    private static boolean isPathCharacter(char character) {
        return isNamespaceCharacter(character) || character == '/';
    }
}
