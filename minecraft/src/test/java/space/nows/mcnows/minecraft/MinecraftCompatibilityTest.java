package space.nows.mcnows.minecraft;

import org.junit.jupiter.api.Test;
import space.nows.mcnows.api.NowsSide;
import space.nows.mcnows.core.mod.ModContainer;
import space.nows.mcnows.core.mod.ModDescriptor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MinecraftCompatibilityTest {
    @Test
    void acceptsBothSideModsOnClientRuntime() {
        ModContainer mod = mod("common_mod", NowsSide.BOTH);

        assertDoesNotThrow(() -> MinecraftCompatibility.validate(List.of(mod), "26.2", NowsSide.CLIENT));
    }

    @Test
    void rejectsServerOnlyModsOnClientRuntime() {
        ModContainer mod = mod("server_mod", NowsSide.SERVER);

        assertThrows(IOException.class, () ->
                MinecraftCompatibility.validate(List.of(mod), "26.2", NowsSide.CLIENT));
    }

    private static ModContainer mod(String id, NowsSide side) {
        return new ModContainer(
                Path.of("mods/" + id + ".jar"),
                new ModDescriptor(
                        id,
                        id,
                        "1.0.0",
                        "26.2",
                        side,
                        "",
                        List.of(),
                        List.of(),
                        List.of(),
                        "",
                        Map.of(),
                        Map.of(),
                        List.of(),
                        Map.of("entrypoint", List.of(id + ".Mod"))));
    }
}
