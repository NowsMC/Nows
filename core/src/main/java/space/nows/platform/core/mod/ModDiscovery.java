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

package space.nows.platform.core.mod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ModDiscovery {
    private ModDiscovery() {}

    public static List<ModContainer> scan(Path modsDirectory, ModMetadataReader reader) throws IOException {
        List<ModContainer> result = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        scanInto(modsDirectory, reader, result, ids);
        return List.copyOf(result);
    }

    public static List<ModContainer> scan(List<Path> modsDirectories, ModMetadataReader reader) throws IOException {
        List<ModContainer> result = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Set<Path> scanned = new HashSet<>();
        for (Path modsDirectory : modsDirectories) {
            Path normalized = modsDirectory.toAbsolutePath().normalize();
            if (scanned.add(normalized)) {
                scanInto(normalized, reader, result, ids);
            }
        }
        return List.copyOf(result);
    }

    private static void scanInto(
            Path modsDirectory,
            ModMetadataReader reader,
            List<ModContainer> result,
            Set<String> ids
    ) throws IOException {
        Files.createDirectories(modsDirectory);
        try (var paths = Files.list(modsDirectory)) {
            for (Path jar : paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList()) {
                var descriptor = reader.read(jar);
                if (descriptor.isEmpty()) continue;
                if (!ids.add(descriptor.get().id())) {
                    throw new IOException("Duplicate Nows mod id '" + descriptor.get().id() + "' in " + jar);
                }
                result.add(new ModContainer(jar.toAbsolutePath().normalize(), descriptor.get()));
            }
        }
    }
}
