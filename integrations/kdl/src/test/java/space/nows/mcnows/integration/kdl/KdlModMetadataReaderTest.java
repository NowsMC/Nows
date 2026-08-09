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
                    description "Optional public helper APIs for Nows mods."
                    author "TamKungZ_"
                    contributor "HollZaterQ"
                    license "Apache-2.0"
                    icon "assets/nows/icon.png"
                    contact homepage="https://nows.space" sources="https://github.com/NowsMC/Nows"
                    depends "nows_example" version=">=1.0.0" reason="Used by this fixture"
                    recommends "modmenu" version="*"
                    property "channel" "dev"
                    entrypoint "space.nows.mod.api.NowsApiMod"
                    mixin "nows_api_mod.mixins.json"
                    api-feature "client-ui"
                    api-feature "modmenu"
                    modmenu "space.nows.mod.api.client.NowsApiModMenu"
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
        assertEquals(2, descriptor.dependencies().size());
        assertEquals("depends", descriptor.dependencies().get(0).kind());
        assertEquals("nows_example", descriptor.dependencies().get(0).id());
        assertEquals(">=1.0.0", descriptor.dependencies().get(0).version());
        assertEquals("Used by this fixture", descriptor.dependencies().get(0).reason());
        assertEquals("recommends", descriptor.dependencies().get(1).kind());
        assertTrue(descriptor.dependencies().get(1).optional());
        assertEquals(List.of("client-ui", "modmenu"), descriptor.declarations("api-feature"));
        assertEquals("client-ui", descriptor.declaration("api-feature").orElseThrow());
        assertEquals(List.of("space.nows.mod.api.client.NowsApiModMenu"), descriptor.declarations("modmenu"));
    }
}
