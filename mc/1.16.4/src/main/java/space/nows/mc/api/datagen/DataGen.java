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

package space.nows.mc.api.datagen;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import space.nows.mc.api.recipe.IngredientSpec;
import space.nows.mc.api.recipe.RecipeSpec;
import space.nows.mc.api.registry.TagSpec;

/** Small generated-data writer for recipes, tags and other JSON assets. */
public interface DataGen {
    Moshi moshi();

    Path outputDirectory();

    Path path(String relativePath);

    void writeText(String relativePath, String text) throws IOException;

    void writeJson(String relativePath, String json) throws IOException;

    @SuppressWarnings({"unchecked", "rawtypes"})
    default void writeJson(String relativePath, Object value) throws IOException {
        // The Object adapter resolves the runtime type but maps concrete collections such as LinkedHashMap to Map.
        writeJson(relativePath, moshi().adapter(Object.class), value);
    }

    default <T> void writeJson(String relativePath, Class<T> type, T value) throws IOException {
        writeJson(relativePath, jsonAdapter(type), value);
    }

    default <T> void writeJson(String relativePath, Type type, T value) throws IOException {
        writeJson(relativePath, jsonAdapter(type), value);
    }

    default <T> void writeJson(String relativePath, JsonAdapter<T> adapter, T value) throws IOException {
        writeJson(relativePath, adapter.indent("  ").toJson(value));
    }

    default <T> JsonAdapter<T> jsonAdapter(Class<T> type) {
        return moshi().adapter(type);
    }

    default <T> JsonAdapter<T> jsonAdapter(Type type) {
        return moshi().adapter(type);
    }

    default String recipePath(String id) {
        return "data/" + namespace(id) + "/recipes/" + pathPart(id) + ".json";
    }

    default void writeTag(TagSpec tag) throws IOException {
        String relativePath = switch (tag.target()) {
            case ITEMS -> itemTagPath(tag.id());
            case BLOCKS -> blockTagPath(tag.id());
            case FLUIDS -> "data/" + namespace(tag.id()) + "/tags/fluids/" + pathPart(tag.id()) + ".json";
            case ENTITY_TYPES -> "data/" + namespace(tag.id()) + "/tags/entity_types/" + pathPart(tag.id()) + ".json";
        };
        writeJson(relativePath, ordered("replace", false, "values", tag.values()));
    }

    default void writeRecipe(RecipeSpec recipe) throws IOException {
        writeJson(recipePath(recipe.id()), recipeJson(recipe));
    }

    default Map<String, Object> recipeJson(RecipeSpec recipe) {
        Map<String, Object> json = new LinkedHashMap<>();
        switch (recipe.kind()) {
            case SHAPED_CRAFTING -> {
                json.put("type", "minecraft:crafting_shaped");
                json.put("pattern", recipe.pattern());
                Map<String, Object> keys = new LinkedHashMap<>();
                recipe.keys().forEach((key, ingredient) -> keys.put(String.valueOf(key), ingredientJson(ingredient)));
                json.put("key", keys);
                json.put("result", stackJson(recipe.result().itemId(), recipe.result().count()));
            }
            case SHAPELESS_CRAFTING -> {
                json.put("type", "minecraft:crafting_shapeless");
                json.put("ingredients", recipe.ingredients().stream().map(DataGen::ingredientJson).toList());
                json.put("result", stackJson(recipe.result().itemId(), recipe.result().count()));
            }
            case SMELTING, BLASTING, SMOKING, CAMPFIRE_COOKING -> {
                json.put("type", "minecraft:" + recipe.kind().name().toLowerCase());
                json.put("ingredient", recipe.ingredients().isEmpty() ? ordered("item", "minecraft:stone") : ingredientJson(recipe.ingredients().get(0)));
                json.put("result", recipe.result().itemId());
                json.put("experience", recipe.experience());
                json.put("cookingtime", recipe.cookingTime());
            }
            case STONECUTTING -> {
                json.put("type", "minecraft:stonecutting");
                json.put("ingredient", recipe.ingredients().isEmpty() ? ordered("item", "minecraft:stone") : ingredientJson(recipe.ingredients().get(0)));
                json.put("result", recipe.result().itemId());
                json.put("count", recipe.result().count());
            }
        }
        return json;
    }

    static Map<String, Object> ingredientJson(IngredientSpec ingredient) {
        if (ingredient.tagId() != null) {
            return ordered("tag", ingredient.tagId());
        }
        if (ingredient.itemIds().size() == 1) {
            return ordered("item", ingredient.itemIds().get(0));
        }
        List<Map<String, Object>> values = new ArrayList<>();
        ingredient.itemIds().forEach(id -> values.add(ordered("item", id)));
        return ordered("items", values);
    }

    static Map<String, Object> stackJson(String itemId, int count) {
        return count == 1 ? ordered("item", itemId) : ordered("item", itemId, "count", count);
    }

    /** Insertion ordered map, because Map.of iterates in a different order on every JVM run. */
    private static Map<String, Object> ordered(Object... keysAndValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int index = 0; index < keysAndValues.length; index += 2) {
            map.put((String) keysAndValues[index], keysAndValues[index + 1]);
        }
        return map;
    }

    default String itemTagPath(String id) {
        return "data/" + namespace(id) + "/tags/items/" + pathPart(id) + ".json";
    }

    default String blockTagPath(String id) {
        return "data/" + namespace(id) + "/tags/blocks/" + pathPart(id) + ".json";
    }

    private static String namespace(String id) {
        int split = id.indexOf(':');
        return split >= 0 ? id.substring(0, split) : "minecraft";
    }

    private static String pathPart(String id) {
        int split = id.indexOf(':');
        return split >= 0 ? id.substring(split + 1) : id;
    }
}
