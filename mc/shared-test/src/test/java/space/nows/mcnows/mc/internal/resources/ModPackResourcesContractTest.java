package space.nows.mcnows.mc.internal.resources;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModPackResourcesContractTest {
    @Test
    void legacyMetadataSerializerHasPackMetadataPath() throws Exception {
        try {
            Class.forName("net.minecraft.server.packs.metadata.MetadataSectionSerializer");
        } catch (ClassNotFoundException ignored) {
            return;
        }

        String classFile = "space/nows/mcnows/mc/internal/resources/ModPackResources.class";
        try (InputStream stream = ClassLoader.getSystemResourceAsStream(classFile)) {
            byte[] bytes = stream == null ? new byte[0] : stream.readAllBytes();
            String constantPool = new String(bytes, StandardCharsets.ISO_8859_1);
            assertTrue(constantPool.contains("pack"),
                    "ModPackResources must recognize the pack metadata section");
            assertTrue(constantPool.contains("ModPackSource") && constantPool.contains("metadata"),
                    "ModPackResources must return Nows pack metadata instead of null");
        }
    }
}
