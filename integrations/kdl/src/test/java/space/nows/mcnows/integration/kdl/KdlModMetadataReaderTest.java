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

package space.nows.mcnows.integration.kdl;

import dev.kdl.parse.KdlParser;
import org.junit.jupiter.api.Test;
import space.nows.mcnows.api.NowsSide;
import space.nows.mcnows.core.mod.ModDescriptor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KdlModMetadataReaderTest {
    @Test
    void keepsFutureDeclarationsAsGenericMetadata() throws Exception {
        String metadata = """
                mod id="nows_api_mod" name="Nows API Mod" version="1.0.0" minecraft="26.2" side="client" {
                    info {
                        description "Optional public helper APIs for Nows mods."
                        author "TamKungZ_"
                        contributor "HollZaterQ"
                        license "Apache-2.0"
                        icon "assets/nows/icon.png"
                    }

                    links {
                        homepage "https://nows.space"
                        sources "https://github.com/NowsMC/Nows"
                    }

                    compatibility {
                        requires "minecraft" version=">=26.2"
                        depends "nows_example" version=">=1.0.0" reason="Used by this fixture"
                        recommends "modmenu" version="*"
                        incompatible-with "bad_mod" reason="Known broken integration"
                    }

                    load-order {
                        after "cloth-config"
                        before "late_mod"
                    }

                    properties {
                        channel "dev"
                    }

                    runtime {
                        entrypoint "space.nows.mod.api.NowsApiMod"
                        mixin "nows_api_mod.mixins.json"
                    }

                    features {
                        api-feature "client-ui"
                        future-feature "space.nows.example.FutureFeature"
                    }
                }
                """;

        ModDescriptor descriptor = KdlModMetadataReader.parse(
                KdlParser.v2().parse(new ByteArrayInputStream(metadata.getBytes(StandardCharsets.UTF_8))),
                Path.of("nows.mod.kdl"));

        assertEquals("Optional public helper APIs for Nows mods.", descriptor.description());
        assertEquals(NowsSide.CLIENT, descriptor.side());
        assertEquals(List.of("TamKungZ_"), descriptor.authors());
        assertEquals(List.of("HollZaterQ"), descriptor.contributors());
        assertEquals(List.of("Apache-2.0"), descriptor.licenses());
        assertEquals("assets/nows/icon.png", descriptor.icon());
        assertEquals("https://nows.space", descriptor.contact("homepage").orElseThrow());
        assertEquals("https://github.com/NowsMC/Nows", descriptor.contact("sources").orElseThrow());
        assertEquals("dev", descriptor.property("channel").orElseThrow());
        assertEquals(6, descriptor.dependencies().size());
        assertEquals("requires", descriptor.dependencies().get(0).kind());
        assertEquals("minecraft", descriptor.dependencies().get(0).id());
        assertEquals("depends", descriptor.dependencies().get(1).kind());
        assertEquals("nows_example", descriptor.dependencies().get(1).id());
        assertEquals(">=1.0.0", descriptor.dependencies().get(1).version());
        assertEquals("Used by this fixture", descriptor.dependencies().get(1).reason());
        assertEquals("recommends", descriptor.dependencies().get(2).kind());
        assertTrue(descriptor.dependencies().get(2).optional());
        assertEquals("incompatible-with", descriptor.dependencies().get(3).kind());
        assertEquals("after", descriptor.dependencies().get(4).kind());
        assertEquals("cloth-config", descriptor.dependencies().get(4).id());
        assertEquals("before", descriptor.dependencies().get(5).kind());
        assertEquals(List.of("client-ui"), descriptor.declarations("api-feature"));
        assertEquals("client-ui", descriptor.declaration("api-feature").orElseThrow());
        assertEquals(List.of("space.nows.example.FutureFeature"), descriptor.declarations("future-feature"));
    }
}
