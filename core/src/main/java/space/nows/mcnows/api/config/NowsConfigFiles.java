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

package space.nows.mcnows.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Small per-mod config file helper backed by the game directory. */
public final class NowsConfigFiles {
    private final Path directory;

    public NowsConfigFiles(Path directory) {
        this.directory = directory;
    }

    public Path directory() {
        return directory;
    }

    public Path modDirectory(String modId) {
        return directory.resolve(modId);
    }

    public Path file(String modId, String name) {
        String fileName = name.endsWith(".properties") ? name : name + ".properties";
        return modDirectory(modId).resolve(fileName);
    }

    public Properties loadProperties(String modId, String name) throws IOException {
        Path file = file(modId, name);
        Properties properties = new Properties();
        if (Files.isRegularFile(file)) {
            try (InputStream input = Files.newInputStream(file)) {
                properties.load(input);
            }
        }
        return properties;
    }

    public void saveProperties(String modId, String name, Properties properties, String comment) throws IOException {
        Path file = file(modId, name);
        Files.createDirectories(file.getParent());
        try (OutputStream output = Files.newOutputStream(file)) {
            properties.store(output, comment);
        }
    }
}
