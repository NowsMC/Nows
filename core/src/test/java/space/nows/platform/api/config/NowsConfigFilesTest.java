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

package space.nows.platform.api.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NowsConfigFilesTest {
    @Test
    void resolvesModPropertiesUnderConfigDirectory(@TempDir Path tempDirectory) {
        NowsConfigFiles configs = new NowsConfigFiles(tempDirectory);

        assertEquals(
                tempDirectory.toAbsolutePath().normalize().resolve("example").resolve("client.properties"),
                configs.file("example", "client"));
        assertEquals(
                tempDirectory.toAbsolutePath().normalize().resolve("example").resolve("server.properties"),
                configs.file("example", "server.properties"));
    }

    @Test
    void rejectsPathTraversalSegments(@TempDir Path tempDirectory) {
        NowsConfigFiles configs = new NowsConfigFiles(tempDirectory);

        assertThrows(IllegalArgumentException.class, () -> configs.file("../outside", "client"));
        assertThrows(IllegalArgumentException.class, () -> configs.file("example", "../outside"));
        assertThrows(IllegalArgumentException.class, () -> configs.file("example/sub", "client"));
        assertThrows(IllegalArgumentException.class, () -> configs.file("example", ""));
    }
}
