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

import java.io.IOException;
import java.nio.file.Path;
import java.util.jar.JarFile;

public final class GameJarLocator {
    private GameJarLocator() {}

    public static Path locateClientJar() throws IOException {
        for (String element : System.getProperty("java.class.path", "").split(java.io.File.pathSeparator)) {
            Path path = Path.of(element);
            if (!java.nio.file.Files.isRegularFile(path) || !element.endsWith(".jar")) continue;
            try (JarFile jar = new JarFile(path.toFile())) {
                if (jar.getEntry("net/minecraft/client/main/Main.class") != null) return path.toAbsolutePath().normalize();
            } catch (IOException ignored) { }
        }
        throw new IOException("Could not locate the Minecraft client jar on java.class.path");
    }
}
