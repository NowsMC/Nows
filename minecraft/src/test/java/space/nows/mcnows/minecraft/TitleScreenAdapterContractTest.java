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

package space.nows.mcnows.minecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TitleScreenAdapterContractTest {
    @Test
    void everyMinecraftAdapterOwnsTheNowsModsTitleScreenAction() throws IOException {
        Path root = repositoryRoot();
        Path mcRoot = root.resolve("mc");

        for (String version : minecraftVersions(mcRoot)) {
            Path javaRoot = mcRoot.resolve(version).resolve("src/main/java/space/nows/mcnows/mc");
            String screenContext = read(javaRoot.resolve("api/client/ui/ScreenContext.java"));
            String loaderMenu = read(javaRoot.resolve("internal/client/LoaderMenu.java"));
            String modListScreen = read(javaRoot.resolve("internal/client/ModListScreen.java"));
            Path mixinRoot = javaRoot.resolve("internal/mixin");
            List<String> clientMixins = clientMixins(mcRoot, version);

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

            if (version.equals("1.21.11")) {
                assertFalse(modListScreen.contains("renderBackground(graphics, mouseX, mouseY, delta)"),
                        version + " ModListScreen must not request a second 1.21.11 GUI blur pass");
            }

            for (String mixin : clientMixins) {
                assertTrue(Files.exists(mixinRoot.resolve(mixin + ".java")),
                        version + " client mixin config must only reference existing source: " + mixin);
            }

            if (clientMixins.contains("TitleScreenMixin")) {
                assertTitleScreenMixinStrategy(version, mixinRoot);
            } else if (clientMixins.contains("ScreenAccessor")
                    && clientMixins.contains("ScreenInitMixin")
                    && clientMixins.contains("ScreenRenderMixin")) {
                assertScreenHookStrategy(version, mixinRoot);
            } else {
                fail(version + " must register a known Nows-owned title-screen integration strategy");
            }
        }
    }

    private static List<String> minecraftVersions(Path mcRoot) throws IOException {
        try (Stream<Path> paths = Files.list(mcRoot)) {
            return paths
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .filter(version -> Files.exists(mixinConfigPath(mcRoot, version)))
                    .sorted()
                    .toList();
        }
    }

    private static List<String> clientMixins(Path mcRoot, String version) throws IOException {
        JsonObject config = JsonParser.parseString(read(mixinConfigPath(mcRoot, version))).getAsJsonObject();
        JsonArray client = config.getAsJsonArray("client");
        return client.asList().stream()
                .map(JsonElement::getAsString)
                .toList();
    }

    private static void assertTitleScreenMixinStrategy(String version, Path mixinRoot) throws IOException {
        String titleScreenMixin = read(mixinRoot.resolve("TitleScreenMixin.java"));

        assertTrue(titleScreenMixin.contains("new space.nows.mcnows.mc.internal.client.ModListScreen(screen, context)"),
                version + " TitleScreenMixin must bind the current title screen as ModListScreen parent");

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

    private static void assertScreenHookStrategy(String version, Path mixinRoot) throws IOException {
        String screenAccessor = read(mixinRoot.resolve("ScreenAccessor.java"));
        String screenInitMixin = read(mixinRoot.resolve("ScreenInitMixin.java"));
        String screenRenderMixin = read(mixinRoot.resolve("ScreenRenderMixin.java"));

        assertTrue(screenAccessor.contains("@Invoker(\"addRenderableWidget\")"),
                version + " ScreenAccessor must expose Screen.addRenderableWidget");
        assertTrue(screenInitMixin.contains("this instanceof TitleScreen"),
                version + " ScreenInitMixin must attach buttons only to the title screen");
        assertTrue(screenInitMixin.contains("method = \"init(II)V\""),
                version + " ScreenInitMixin must hook the mapped Screen.init(int,int) lifecycle");
        assertTrue(screenInitMixin.contains("titleScreenImpl().addButtons"),
                version + " ScreenInitMixin must initialize Nows title-screen widgets");
        assertTrue(screenInitMixin.contains("new ModListScreen(screen, context)"),
                version + " ScreenInitMixin must bind the current title screen as ModListScreen parent");
        assertTrue(screenRenderMixin.contains("this instanceof TitleScreen"),
                version + " ScreenRenderMixin must render only on the title screen");
        assertTrue(screenRenderMixin.contains("titleScreenImpl().renderAll"),
                version + " ScreenRenderMixin must render Nows title-screen widgets");
        assertFalse(Files.exists(mixinRoot.resolve("TitleScreenMixin.java")),
                version + " screen-hook strategy must not ship the direct TitleScreen mixin");
    }

    private static Path mixinConfigPath(Path mcRoot, String version) {
        return mcRoot.resolve(version)
                .resolve("src/main/resources/nows_mc_" + version.replace('.', '_') + ".mixins.json");
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
