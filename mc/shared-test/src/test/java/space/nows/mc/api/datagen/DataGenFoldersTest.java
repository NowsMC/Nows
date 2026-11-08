package space.nows.mc.api.datagen;

import com.squareup.moshi.Moshi;
import org.junit.jupiter.api.Test;
import space.nows.mc.api.registry.TagSpec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Minecraft renamed its data pack folders in 1.21 (recipes to recipe, tags/items to tags/item and so on). The vanilla
 * data on the test classpath says which names this adapter's Minecraft reads, and generated files must use them.
 */
class DataGenFoldersTest {
    private final List<String> written = new ArrayList<>();
    private final DataGen dataGen = new DataGen() {
        @Override
        public Moshi moshi() {
            return new Moshi.Builder().build();
        }

        @Override
        public Path outputDirectory() {
            return Path.of(".");
        }

        @Override
        public Path path(String relativePath) {
            return Path.of(relativePath);
        }

        @Override
        public void writeText(String relativePath, String text) throws IOException {
            written.add(relativePath);
        }

        @Override
        public void writeJson(String relativePath, String json) throws IOException {
            written.add(relativePath);
        }
    };

    /** Returns the folder that holds the given vanilla file, trying the old plural name first. */
    private static String vanillaFolder(String parent, String plural, String singular, String file) {
        boolean oldNames = DataGenFoldersTest.class.getResource("/data/minecraft/" + parent + plural + "/" + file) != null;
        boolean newNames = DataGenFoldersTest.class.getResource("/data/minecraft/" + parent + singular + "/" + file) != null;
        assertNotNull(oldNames || newNames ? "found" : null, "vanilla " + file + " not found under " + parent + plural + " or " + singular);
        return oldNames ? plural : singular;
    }

    @Test
    void recipesGoWhereThisMinecraftReadsThem() {
        String folder = vanillaFolder("", "recipes", "recipe", "acacia_boat.json");

        assertEquals("data/my_mod/" + folder + "/widget.json", dataGen.recipePath("my_mod:widget"));
    }

    @Test
    void tagsGoWhereThisMinecraftReadsThem() throws IOException {
        String items = vanillaFolder("tags/", "items", "item", "planks.json");
        String blocks = vanillaFolder("tags/", "blocks", "block", "planks.json");
        String fluids = vanillaFolder("tags/", "fluids", "fluid", "water.json");
        String entityTypes = vanillaFolder("tags/", "entity_types", "entity_type", "skeletons.json");

        dataGen.writeTag(TagSpec.items("my_mod:things", List.of("minecraft:stone")));
        dataGen.writeTag(TagSpec.blocks("my_mod:things", List.of("minecraft:stone")));
        dataGen.writeTag(new TagSpec(TagSpec.TagTarget.FLUIDS, "my_mod:things", List.of("minecraft:water")));
        dataGen.writeTag(new TagSpec(TagSpec.TagTarget.ENTITY_TYPES, "my_mod:things", List.of("minecraft:pig")));

        assertEquals(List.of(
                "data/my_mod/tags/" + items + "/things.json",
                "data/my_mod/tags/" + blocks + "/things.json",
                "data/my_mod/tags/" + fluids + "/things.json",
                "data/my_mod/tags/" + entityTypes + "/things.json"), written);
    }
}
