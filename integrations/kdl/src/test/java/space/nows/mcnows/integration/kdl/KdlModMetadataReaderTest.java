package space.nows.mcnows.integration.kdl;

import dev.kdl.parse.KdlParser;
import org.junit.jupiter.api.Test;
import space.nows.mcnows.core.mod.ModDescriptor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KdlModMetadataReaderTest {
    @Test
    void keepsFutureDeclarationsAsGenericMetadata() throws Exception {
        String metadata = """
                mod id="nows_api_mod" name="Nows API Mod" version="1.0.0" minecraft="26.2" {
                    entrypoint "space.nows.mcnows.apimod.NowsApiMod"
                    mixin "nows_api_mod.mixins.json"
                    api-feature "client-ui"
                    api-feature "modmenu"
                    modmenu "space.nows.mcnows.apimod.client.NowsApiModMenu"
                }
                """;

        ModDescriptor descriptor = KdlModMetadataReader.parse(
                KdlParser.v2().parse(new ByteArrayInputStream(metadata.getBytes(StandardCharsets.UTF_8))),
                Path.of("nows.mod.kdl"));

        assertEquals(List.of("client-ui", "modmenu"), descriptor.declarations("api-feature"));
        assertEquals(List.of("space.nows.mcnows.apimod.client.NowsApiModMenu"), descriptor.declarations("modmenu"));
    }
}
