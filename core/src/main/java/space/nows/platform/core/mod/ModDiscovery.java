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
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class ModDiscovery {
    private ModDiscovery() {}

    public interface ScanObserver {
        ScanObserver NONE = new ScanObserver() {};

        default void onDirectory(Path modsDirectory, int candidateCount) {}
        default void onCandidate(Path jar, int index, int total) {}
        default void onDiscovered(ModContainer mod) {}
        default void onIgnored(Path jar) {}
    }

    public static List<ModContainer> scan(Path modsDirectory, ModMetadataReader reader) throws IOException {
        Objects.requireNonNull(reader, "reader");
        List<ModContainer> result = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        scanInto(Objects.requireNonNull(modsDirectory, "modsDirectory"), reader, result, ids, ScanObserver.NONE);
        return List.copyOf(result);
    }

    public static List<ModContainer> scan(List<Path> modsDirectories, ModMetadataReader reader) throws IOException {
        return scan(modsDirectories, reader, ScanObserver.NONE);
    }

    public static List<ModContainer> scan(List<Path> modsDirectories, ModMetadataReader reader, ScanObserver observer)
            throws IOException {
        Objects.requireNonNull(modsDirectories, "modsDirectories");
        Objects.requireNonNull(reader, "reader");
        Objects.requireNonNull(observer, "observer");
        List<ModContainer> result = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Set<Path> scanned = new HashSet<>();
        for (Path modsDirectory : modsDirectories) {
            Path normalized = Objects.requireNonNull(modsDirectory, "modsDirectory")
                    .toAbsolutePath()
                    .normalize();
            if (scanned.add(normalized)) {
                scanInto(normalized, reader, result, ids, observer);
            }
        }
        return List.copyOf(result);
    }

    private static void scanInto(
            Path modsDirectory,
            ModMetadataReader reader,
            List<ModContainer> result,
            Set<String> ids,
            ScanObserver observer
    ) throws IOException {
        Files.createDirectories(modsDirectory);
        try (var paths = Files.list(modsDirectory)) {
            List<Path> jars = paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .toList();
            observer.onDirectory(modsDirectory, jars.size());
            for (int index = 0; index < jars.size(); index++) {
                Path jar = jars.get(index);
                observer.onCandidate(jar, index + 1, jars.size());
                var descriptor = reader.read(jar);
                if (descriptor.isEmpty()) {
                    observer.onIgnored(jar);
                    continue;
                }
                if (!ids.add(descriptor.get().id())) {
                    throw new IOException("Duplicate Nows mod id '" + descriptor.get().id() + "' in " + jar);
                }
                ModContainer mod = new ModContainer(jar.toAbsolutePath().normalize(), descriptor.get());
                result.add(mod);
                observer.onDiscovered(mod);
            }
        }
    }
}
