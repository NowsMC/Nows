package space.nows.mcnows.minecraft;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TitleScreenAdapterContractTest {
    @Test
    void everyMinecraftAdapterOwnsTheNowsModsTitleScreenAction() throws IOException {
        Path root = repositoryRoot();
        Path mcRoot = root.resolve("mc");

        for (String version : minecraftVersions(mcRoot)) {
            Path javaRoot = mcRoot.resolve(version).resolve("src/main/java/space/nows/mcnows/mc");
            String screenContext = read(javaRoot.resolve("api/client/ui/ScreenContext.java"));
            String loaderMenu = read(javaRoot.resolve("internal/client/LoaderMenu.java"));
            String titleScreenMixin = read(javaRoot.resolve("internal/mixin/TitleScreenMixin.java"));
            String mixinConfig = read(mcRoot.resolve(version)
                    .resolve("src/main/resources/nows_mc_" + version.replace('.', '_') + ".mixins.json"));

            assertTrue(screenContext.contains("Consumer<NowsContext> nowsMods"),
                    version + " ScreenContext must carry a Nows-owned title-screen action");
            assertTrue(screenContext.contains("public void showNowsMods(NowsContext context)"),
                    version + " ScreenContext must expose showNowsMods");

            assertTrue(loaderMenu.contains("title.showNowsMods(context)"),
                    version + " LoaderMenu must use the adapter-provided Nows Mods action");
            assertFalse(loaderMenu.contains("new TitleScreen"),
                    version + " LoaderMenu must not construct Minecraft TitleScreen directly");
            assertFalse(loaderMenu.contains("Minecraft.getInstance()"),
                    version + " LoaderMenu must not own version-specific screen switching");

            assertTrue(titleScreenMixin.contains("new space.nows.mcnows.mc.internal.client.ModListScreen(screen, context)"),
                    version + " TitleScreenMixin must bind the current title screen as ModListScreen parent");
            assertTrue(mixinConfig.contains("\"TitleScreenMixin\""),
                    version + " built-in mixin config must register TitleScreenMixin");

            if (version.startsWith("26.")) {
                assertTrue(titleScreenMixin.contains("setScreenAndShow"),
                        version + " should use the newer screen show API");
            } else {
                assertTrue(titleScreenMixin.contains(".setScreen("),
                        version + " should use the classic screen switch API");
                assertFalse(titleScreenMixin.contains("setScreenAndShow"),
                        version + " should not use the newer screen show API");
            }
        }
    }

    private static List<String> minecraftVersions(Path mcRoot) throws IOException {
        try (Stream<Path> paths = Files.list(mcRoot)) {
            return paths
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .filter(version -> Files.exists(pathForVersion(mcRoot, version)))
                    .sorted()
                    .toList();
        }
    }

    private static Path pathForVersion(Path mcRoot, String version) {
        return mcRoot.resolve(version)
                .resolve("src/main/java/space/nows/mcnows/mc/internal/mixin/TitleScreenMixin.java");
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.getParent();
        }
        if (current == null) {
            throw new IllegalStateException("Could not locate repository root");
        }
        return current;
    }
}
