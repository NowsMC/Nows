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
import space.nows.platform.api.NowsSide;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModDependencyResolverTest {
    @Test
    void validatesRequiredProvidedVersions() throws Exception {
        ModContainer mod = mod("example", "1.0.0",
                List.of(new ModDependency("requires", "minecraft", ">=26.2", false, "")));

        List<ModContainer> resolved = ModDependencyResolver.resolve(
                List.of(mod),
                Map.of("minecraft", "26.2"));

        assertEquals(List.of(mod), resolved);
    }

    @Test
    void rejectsMissingRequiredMods() {
        ModContainer mod = mod("example", "1.0.0",
                List.of(new ModDependency("depends", "cloth-config", ">=1.0.0", false, "")));

        assertThrows(IOException.class, () -> ModDependencyResolver.resolve(List.of(mod), Map.of()));
    }

    @Test
    void rejectsConflictingLoadedMods() {
        ModContainer example = mod("example", "1.0.0",
                List.of(new ModDependency("incompatible-with", "bad_mod", "*", false, "")));
        ModContainer bad = mod("bad_mod", "1.0.0", List.of());

        assertThrows(IOException.class, () -> ModDependencyResolver.resolve(List.of(example, bad), Map.of()));
    }

    @Test
    void sortsByRequiredDependenciesAndLoadRules() throws Exception {
        ModContainer first = mod("first", "1.0.0", List.of());
        ModContainer middle = mod("middle", "1.0.0",
                List.of(new ModDependency("load-after", "first", "*", false, "")));
        ModContainer last = mod("last", "1.0.0",
                List.of(new ModDependency("depends", "middle", "*", false, "")));

        List<ModContainer> resolved = ModDependencyResolver.resolve(List.of(last, middle, first), Map.of());

        assertEquals(List.of(first, middle, last), resolved);
    }

    private static ModContainer mod(String id, String version, List<ModDependency> dependencies) {
        return new ModContainer(
                Path.of("mods/" + id + ".jar"),
                new ModDescriptor(
                        id,
                        id,
                        version,
                        "26.2",
                        NowsSide.BOTH,
                        "",
                        List.of(),
                        List.of(),
                        List.of(),
                        "",
                        Map.of(),
                        Map.of(),
                        dependencies,
                        Map.of("entrypoint", List.of(id + ".Mod"))));
    }
}
