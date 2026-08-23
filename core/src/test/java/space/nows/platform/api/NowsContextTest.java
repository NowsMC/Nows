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

package space.nows.platform.api;

import org.junit.jupiter.api.Test;
import space.nows.platform.core.mod.ModContainer;
import space.nows.platform.core.mod.ModDescriptor;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NowsContextTest {
    @Test
    void exposesModsByIdForModDevelopers() {
        ModDescriptor descriptor = new ModDescriptor(
                "nows_example",
                "Nows Example Mod",
                "1.0.0",
                "26.2",
                Map.of("entrypoint", List.of("example.Mod")));
        ModContainer container = new ModContainer(Path.of("mods/nows-example.jar"), descriptor);
        NowsContext context = new NowsContext(
                "26.2",
                NowsSide.CLIENT,
                Path.of(".minecraft"),
                List.of(container),
                getClass().getClassLoader(),
                new NowsServices());

        assertTrue(context.isModLoaded("nows_example"));
        assertEquals(NowsSide.CLIENT, context.side());
        assertEquals("Nows Example Mod", context.requireModDescriptor("nows_example").name());
        assertEquals("Nows Example Mod", context.requireModDescriptor(" Nows_Example ").name());
        assertEquals(container, context.requireMod("nows_example"));
        assertEquals(List.of(descriptor), context.modDescriptors());
        assertEquals(container, context.modsById().get("nows_example"));
    }

    @Test
    void rejectsDuplicateModIds() {
        ModContainer first = container("same_id");
        ModContainer second = container("same_id");

        assertThrows(IllegalArgumentException.class, () -> new NowsContext(
                "26.2",
                NowsSide.CLIENT,
                Path.of(".minecraft"),
                List.of(first, second),
                getClass().getClassLoader(),
                new NowsServices()));
    }

    private static ModContainer container(String id) {
        return new ModContainer(
                Path.of("mods/" + id + ".jar"),
                new ModDescriptor(id, id, "1.0.0", "26.2", Map.of()));
    }
}
