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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModDiscoveryTest {
    @Test
    void scansJarsDeterministically(@TempDir Path tempDirectory) throws Exception {
        Path mods = tempDirectory.resolve("mods");
        Files.createDirectories(mods);
        Files.createFile(mods.resolve("b.JAR"));
        Files.createFile(mods.resolve("a.jar"));
        Files.createFile(mods.resolve("ignored.txt"));

        List<ModContainer> discovered = ModDiscovery.scan(mods, jar -> Optional.of(descriptor(
                jar.getFileName().toString().substring(0, 1))));

        assertEquals(List.of("a", "b"), discovered.stream().map(mod -> mod.descriptor().id()).toList());
        assertEquals(mods.resolve("a.jar").toAbsolutePath().normalize(), discovered.get(0).path());
    }

    @Test
    void scansEachDirectoryOnce(@TempDir Path tempDirectory) throws Exception {
        Path mods = tempDirectory.resolve("mods");
        Files.createDirectories(mods);
        Files.createFile(mods.resolve("example.jar"));

        List<ModContainer> discovered = ModDiscovery.scan(
                List.of(mods, mods.resolve(".").normalize()),
                jar -> Optional.of(descriptor("example")));

        assertEquals(1, discovered.size());
    }

    @Test
    void rejectsDuplicateModIdsAcrossDirectories(@TempDir Path tempDirectory) throws Exception {
        Path main = tempDirectory.resolve("main");
        Path overlay = tempDirectory.resolve("overlay");
        Files.createDirectories(main);
        Files.createDirectories(overlay);
        Files.createFile(main.resolve("first.jar"));
        Files.createFile(overlay.resolve("second.jar"));

        assertThrows(IOException.class, () -> ModDiscovery.scan(
                List.of(main, overlay),
                jar -> Optional.of(descriptor("duplicate"))));
    }

    @Test
    void reportsScanProgressAndIgnoredJarCandidates(@TempDir Path tempDirectory) throws Exception {
        Path mods = tempDirectory.resolve("mods");
        Files.createDirectories(mods);
        Files.createFile(mods.resolve("a.jar"));
        Files.createFile(mods.resolve("ignored.jar"));

        AtomicInteger directories = new AtomicInteger();
        AtomicInteger candidates = new AtomicInteger();
        AtomicInteger discovered = new AtomicInteger();
        AtomicInteger ignored = new AtomicInteger();

        List<ModContainer> result = ModDiscovery.scan(List.of(mods), jar -> {
            if (jar.getFileName().toString().equals("ignored.jar")) {
                return Optional.empty();
            }
            return Optional.of(descriptor("a"));
        }, new ModDiscovery.ScanObserver() {
            @Override
            public void onDirectory(Path modsDirectory, int candidateCount) {
                directories.incrementAndGet();
                assertEquals(2, candidateCount);
            }

            @Override
            public void onCandidate(Path jar, int index, int total) {
                candidates.incrementAndGet();
                assertEquals(2, total);
            }

            @Override
            public void onDiscovered(ModContainer mod) {
                discovered.incrementAndGet();
            }

            @Override
            public void onIgnored(Path jar) {
                ignored.incrementAndGet();
            }
        });

        assertEquals(1, result.size());
        assertEquals(1, directories.get());
        assertEquals(2, candidates.get());
        assertEquals(1, discovered.get());
        assertEquals(1, ignored.get());
    }

    private static ModDescriptor descriptor(String id) {
        return new ModDescriptor(id, id, "1.0.0", "26.2", Map.of());
    }
}
