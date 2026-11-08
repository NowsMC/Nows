package space.nows.mc.api.datagen;

import org.junit.jupiter.api.Test;
import space.nows.mc.api.recipe.IngredientSpec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DataGenJsonTest {
    @Test
    void generatedJsonObjectsKeepInsertionOrder() {
        // Map.of iterates in a different order on every JVM run, which would reorder keys in generated files.
        assertInstanceOf(LinkedHashMap.class, DataGen.stackJson("my_mod:widget", 2));
        assertInstanceOf(LinkedHashMap.class, DataGen.ingredientJson(IngredientSpec.item("minecraft:stone")));
        assertInstanceOf(LinkedHashMap.class, DataGen.ingredientJson(IngredientSpec.tag("minecraft:planks")));
    }

    @Test
    void stackJsonCarriesTheItemAndOnlyANonDefaultCount() {
        Map<String, Object> single = DataGen.stackJson("my_mod:widget", 1);
        Map<String, Object> several = DataGen.stackJson("my_mod:widget", 3);

        assertEquals(List.of("item"), List.copyOf(single.keySet()));
        assertEquals(List.of("item", "count"), List.copyOf(several.keySet()));
        assertEquals(3, several.get("count"));
    }
}
