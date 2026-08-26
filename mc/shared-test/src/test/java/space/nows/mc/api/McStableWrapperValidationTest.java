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

package space.nows.mc.api;

import org.junit.jupiter.api.Test;
import space.nows.mc.api.data.McDataPatch;
import space.nows.mc.api.recipe.IngredientSpec;
import space.nows.mc.api.recipe.RecipeSpec;
import space.nows.mc.api.recipe.WorkstationRecipeSpec;
import space.nows.mc.api.registry.ItemSpec;
import space.nows.mc.api.registry.McItemStack;
import space.nows.mc.api.registry.TagSpec;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class McStableWrapperValidationTest {
    @Test
    void validatesStableMinecraftIdsBeforeAdaptersSeeThem() {
        assertEquals("minecraft", McId.of("stone").namespace());
        assertEquals("stone", McId.of("stone").path());
        assertEquals("nows:block/path", McId.of("nows:block/path").toString());

        assertThrows(IllegalArgumentException.class, () -> ItemSpec.builder("Nows:Bad").build());
        assertThrows(IllegalArgumentException.class, () -> ItemSpec.builder("nows:bad id").build());
        assertThrows(IllegalArgumentException.class, () -> ItemSpec.builder("nows:bad:id").build());
        assertThrows(IllegalArgumentException.class, () -> ItemSpec.builder(" nows:bad").build());
    }

    @Test
    void validatesCollectionBasedStableSpecs() {
        assertThrows(IllegalArgumentException.class, () -> TagSpec.items("nows:ores", List.of("minecraft:stone", "Bad:Ore")));
        assertThrows(IllegalArgumentException.class, () -> IngredientSpec.items(List.of("minecraft:stone", "minecraft:bad item")));
        assertThrows(IllegalArgumentException.class, () -> McDataPatch.builder().remove("minecraft:bad component"));
    }

    @Test
    void rejectsIncompleteRecipesAtWrapperLayer() {
        assertThrows(IllegalArgumentException.class, () ->
                RecipeSpec.shaped("nows:empty", McItemStack.of("minecraft:stone")).build());
        assertThrows(IllegalArgumentException.class, () ->
                RecipeSpec.shapeless("nows:empty", McItemStack.of("minecraft:stone")).build());
        assertThrows(IllegalArgumentException.class, () ->
                RecipeSpec.smelting("nows:empty", McItemStack.of("minecraft:stone")).build());
        assertThrows(IllegalArgumentException.class, () ->
                RecipeSpec.shapeless("nows:slow", McItemStack.of("minecraft:stone"))
                        .ingredient(IngredientSpec.item("minecraft:cobblestone"))
                        .cookingTime(0)
                        .build());
    }

    @Test
    void rejectsNullRecipeBuilderPartsNearTheCallSite() {
        assertThrows(NullPointerException.class, () ->
                RecipeSpec.shapeless("nows:null", McItemStack.of("minecraft:stone")).ingredient(null));
        assertThrows(NullPointerException.class, () ->
                WorkstationRecipeSpec.builder("nows:null", "nows:workstation").result(null));
        assertThrows(NullPointerException.class, () ->
                McDataPatch.builder().set(null));
    }
}
