package space.nows.mcnows.api;

import org.junit.jupiter.api.Test;
import space.nows.mcnows.core.mod.ModContainer;
import space.nows.mcnows.core.mod.ModDescriptor;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals(container, context.requireMod("nows_example"));
        assertEquals(List.of(descriptor), context.modDescriptors());
        assertEquals(container, context.modsById().get("nows_example"));
    }
}
