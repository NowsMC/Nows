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

package space.nows.mc.api.recipe;

import space.nows.mc.api.registry.ItemSpec;
import space.nows.mc.api.registry.McItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Stable recipe description for generator/datagen code. */
public final class RecipeSpec {
    private final RecipeKind kind;
    private final String id;
    private final McItemStack result;
    private final List<String> pattern;
    private final Map<Character, IngredientSpec> keys;
    private final List<IngredientSpec> ingredients;
    private final float experience;
    private final int cookingTime;

    private RecipeSpec(Builder builder) {
        this.kind = builder.kind;
        this.id = ItemSpec.requireId(builder.id);
        this.result = builder.result;
        this.pattern = List.copyOf(builder.pattern);
        this.keys = Map.copyOf(builder.keys);
        this.ingredients = List.copyOf(builder.ingredients);
        this.experience = builder.experience;
        this.cookingTime = builder.cookingTime;
        if (result == null) {
            throw new IllegalArgumentException("result must not be null");
        }
    }

    public static Builder shaped(String id, McItemStack result) {
        return new Builder(RecipeKind.SHAPED_CRAFTING, id, result);
    }

    public static Builder shapeless(String id, McItemStack result) {
        return new Builder(RecipeKind.SHAPELESS_CRAFTING, id, result);
    }

    public static Builder smelting(String id, McItemStack result) {
        return new Builder(RecipeKind.SMELTING, id, result);
    }

    public static Builder stonecutting(String id, McItemStack result) {
        return new Builder(RecipeKind.STONECUTTING, id, result);
    }

    public RecipeKind kind() {
        return kind;
    }

    public String id() {
        return id;
    }

    public McItemStack result() {
        return result;
    }

    public List<String> pattern() {
        return pattern;
    }

    public Map<Character, IngredientSpec> keys() {
        return keys;
    }

    public List<IngredientSpec> ingredients() {
        return ingredients;
    }

    public float experience() {
        return experience;
    }

    public int cookingTime() {
        return cookingTime;
    }

    public enum RecipeKind {
        SHAPED_CRAFTING,
        SHAPELESS_CRAFTING,
        SMELTING,
        BLASTING,
        SMOKING,
        CAMPFIRE_COOKING,
        STONECUTTING
    }

    public static final class Builder {
        private final RecipeKind kind;
        private final String id;
        private final McItemStack result;
        private final List<String> pattern = new java.util.ArrayList<>();
        private final Map<Character, IngredientSpec> keys = new LinkedHashMap<>();
        private final List<IngredientSpec> ingredients = new java.util.ArrayList<>();
        private float experience;
        private int cookingTime = 200;

        private Builder(RecipeKind kind, String id, McItemStack result) {
            this.kind = kind;
            this.id = id;
            this.result = result;
        }

        public Builder pattern(String row) {
            pattern.add(row);
            return this;
        }

        public Builder key(char key, IngredientSpec ingredient) {
            keys.put(key, ingredient);
            return this;
        }

        public Builder ingredient(IngredientSpec ingredient) {
            ingredients.add(ingredient);
            return this;
        }

        public Builder experience(float experience) {
            this.experience = experience;
            return this;
        }

        public Builder cookingTime(int cookingTime) {
            this.cookingTime = cookingTime;
            return this;
        }

        public RecipeSpec build() {
            return new RecipeSpec(this);
        }
    }
}
