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

package space.nows.mcnows.minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/** Version-specific Minecraft launch policy loaded from mc/<minecraft-version>. */
public record MinecraftVersionPolicy(
        String minecraftVersion,
        String clientMainClass,
        List<String> builtInMixinConfigs,
        String resourcePath,
        boolean bundled) {
    private static final String DEFAULT_CLIENT_MAIN_CLASS = "net.minecraft.client.main.Main";

    public static MinecraftVersionPolicy load(String minecraftVersion) throws IOException {
        String normalized = normalizeVersion(minecraftVersion);
        String resourcePath = "META-INF/nows/mc/" + normalized + "/nows-minecraft.properties";

        Properties properties = new Properties();
        ClassLoader loader = MinecraftVersionPolicy.class.getClassLoader();
        try (InputStream input = loader.getResourceAsStream(resourcePath)) {
            if (input == null) {
                return new MinecraftVersionPolicy(normalized, DEFAULT_CLIENT_MAIN_CLASS, List.of(), resourcePath, false);
            }
            properties.load(input);
        }

        String declaredVersion = read(properties, "minecraft.version", normalized);
        if (!declaredVersion.equals(normalized)) {
            throw new IOException(resourcePath + " declares minecraft.version=" + declaredVersion
                    + " but launcher requested " + normalized);
        }

        return new MinecraftVersionPolicy(
                normalized,
                read(properties, "client.mainClass", DEFAULT_CLIENT_MAIN_CLASS),
                readList(properties, "runtime.builtinMixinConfigs"),
                resourcePath,
                true);
    }

    private static String normalizeVersion(String minecraftVersion) {
        String normalized = Objects.requireNonNull(minecraftVersion, "minecraftVersion").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Minecraft version is blank");
        }
        if (normalized.indexOf('/') >= 0 || normalized.indexOf('\\') >= 0) {
            throw new IllegalArgumentException("Minecraft version must not contain path separators: " + normalized);
        }
        return normalized;
    }

    private static String read(Properties properties, String key, String fallback) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private static List<String> readList(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split(",")).stream()
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}
