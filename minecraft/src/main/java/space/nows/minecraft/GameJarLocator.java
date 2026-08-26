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

package space.nows.minecraft;

import space.nows.platform.api.NowsSide;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;

public final class GameJarLocator {
    private GameJarLocator() {}

    public static Path locate(NowsSide side) throws IOException {
        return side == NowsSide.SERVER ? locateServerJar() : locateClientJar();
    }

    public static Path locateClientJar() throws IOException {
        Path configured = configuredJar("nows.clientJar", "nows.gameJar");
        if (configured != null) {
            return configured;
        }
        return locateOnClasspath(
                List.of("net/minecraft/client/main/Main.class"),
                "Could not locate the Minecraft client jar on java.class.path");
    }

    public static Path locateServerJar() throws IOException {
        Path configured = configuredJar("nows.serverJar", "nows.gameJar");
        if (configured != null) {
            return configured;
        }
        return locateOnClasspath(
                List.of("net/minecraft/server/Main.class", "net/minecraft/bundler/Main.class"),
                "Could not locate the Minecraft server jar on java.class.path");
    }

    private static Path configuredJar(String primaryProperty, String fallbackProperty) throws IOException {
        String configured = System.getProperty(primaryProperty);
        if (configured == null || configured.isBlank()) {
            configured = System.getProperty(fallbackProperty);
        }
        if (configured == null || configured.isBlank()) {
            return null;
        }
        Path path = Path.of(configured).toAbsolutePath().normalize();
        if (!java.nio.file.Files.isRegularFile(path)) {
            throw new IOException("Configured Minecraft jar is missing: " + path);
        }
        return path;
    }

    private static Path locateOnClasspath(List<String> markers, String failureMessage) throws IOException {
        for (String element : System.getProperty("java.class.path", "").split(java.io.File.pathSeparator)) {
            Path path = Path.of(element);
            if (!java.nio.file.Files.isRegularFile(path) || !element.endsWith(".jar")) continue;
            try (JarFile jar = new JarFile(path.toFile())) {
                for (String marker : markers) {
                    if (jar.getEntry(marker) != null) return path.toAbsolutePath().normalize();
                }
            } catch (IOException ignored) { }
        }
        throw new IOException(failureMessage);
    }
}
