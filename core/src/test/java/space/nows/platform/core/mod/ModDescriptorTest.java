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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModDescriptorTest {
    @Test
    void normalizesStableLookupKeys() {
        ModDescriptor descriptor = new ModDescriptor(
                " Example_Mod ",
                " Example ",
                " 1.0.0 ",
                " 26.2 ",
                NowsSide.BOTH,
                " Description ",
                List.of(" Author "),
                List.of(" Contributor "),
                List.of(" Apache-2.0 "),
                " icon.png ",
                Map.of("homepage", " https://nows.space "),
                Map.of("channel", " stable "),
                List.of(new ModDependency("depends", " Required_Mod ", "*", false, "")),
                Map.of(" EntryPoint ", List.of(" example.Mod ", "", "  ")));

        assertEquals("example_mod", descriptor.id());
        assertEquals("Example", descriptor.name());
        assertEquals("1.0.0", descriptor.version());
        assertEquals("26.2", descriptor.minecraft());
        assertEquals(List.of("example.Mod"), descriptor.declarations("entrypoint"));
        assertEquals(List.of("example.Mod"), descriptor.declarations(" ENTRYPOINT "));
        assertEquals(List.of(), descriptor.declarations(null));
        assertEquals("https://nows.space", descriptor.contact("homepage").orElseThrow());
        assertEquals("stable", descriptor.property("channel").orElseThrow());
        assertEquals(1, descriptor.dependencies("required_mod").size());
        assertEquals(1, descriptor.dependencies(" Required_Mod ").size());
    }

    @Test
    void rejectsBlankIds() {
        assertThrows(IllegalArgumentException.class, () -> new ModDescriptor(
                " ",
                "",
                "",
                "",
                Map.of()));
    }
}
