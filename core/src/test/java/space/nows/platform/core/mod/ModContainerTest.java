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

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModContainerTest {
    @Test
    void normalizesModPath() {
        ModDescriptor descriptor = new ModDescriptor("example", "Example", "1.0.0", "26.2", Map.of());
        ModContainer container = new ModContainer(Path.of("mods").resolve("..").resolve("mods/example.jar"), descriptor);

        assertEquals(Path.of("mods/example.jar").toAbsolutePath().normalize(), container.path());
    }

    @Test
    void rejectsNullMembers() {
        ModDescriptor descriptor = new ModDescriptor("example", "Example", "1.0.0", "26.2", Map.of());

        assertThrows(NullPointerException.class, () -> new ModContainer(null, descriptor));
        assertThrows(NullPointerException.class, () -> new ModContainer(Path.of("mods/example.jar"), null));
    }
}
