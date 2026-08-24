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

import java.util.ArrayList;
import java.util.List;

/** Stable custom recipe shape for cooking and workstation mods. */
public final class WorkstationRecipeSpec {
    private final String id;
    private final String typeId;
    private final List<IngredientSpec> ingredients;
    private final IngredientSpec fuel;
    private final List<McItemStack> results;
    private final McItemStack byproduct;
    private final float byproductChance;
    private final float experience;
    private final int cookingTime;
    private final boolean combined;

    private WorkstationRecipeSpec(Builder builder) {
        this.id = ItemSpec.requireId(builder.id);
        this.typeId = ItemSpec.requireId(builder.typeId);
        this.ingredients = List.copyOf(builder.ingredients);
        this.fuel = builder.fuel;
        this.results = List.copyOf(builder.results);
        this.byproduct = builder.byproduct;
        this.byproductChance = builder.byproductChance;
        this.experience = builder.experience;
        this.cookingTime = builder.cookingTime;
        this.combined = builder.combined;
        if (ingredients.isEmpty() && fuel == null) {
            throw new IllegalArgumentException("recipe must have ingredients or fuel");
        }
        if (results.isEmpty() && byproduct == null) {
            throw new IllegalArgumentException("recipe must have results or byproduct");
        }
        if (byproductChance < 0.0F || byproductChance > 1.0F) {
            throw new IllegalArgumentException("byproductChance must be between 0.0 and 1.0");
        }
        if (experience < 0.0F) {
            throw new IllegalArgumentException("experience must be >= 0.0");
        }
        if (cookingTime < 1) {
            throw new IllegalArgumentException("cookingTime must be >= 1");
        }
    }

    public static Builder builder(String id, String typeId) {
        return new Builder(id, typeId);
    }

    public String id() {
        return id;
    }

    public String typeId() {
        return typeId;
    }

    public List<IngredientSpec> ingredients() {
        return ingredients;
    }

    public IngredientSpec fuel() {
        return fuel;
    }

    public List<McItemStack> results() {
        return results;
    }

    public McItemStack byproduct() {
        return byproduct;
    }

    public float byproductChance() {
        return byproductChance;
    }

    public float experience() {
        return experience;
    }

    public int cookingTime() {
        return cookingTime;
    }

    public boolean combined() {
        return combined;
    }

    public static final class Builder {
        private final String id;
        private final String typeId;
        private final List<IngredientSpec> ingredients = new ArrayList<>();
        private IngredientSpec fuel;
        private final List<McItemStack> results = new ArrayList<>();
        private McItemStack byproduct;
        private float byproductChance;
        private float experience;
        private int cookingTime = 100;
        private boolean combined;

        private Builder(String id, String typeId) {
            this.id = id;
            this.typeId = typeId;
        }

        public Builder ingredient(IngredientSpec ingredient) {
            ingredients.add(ingredient);
            return this;
        }

        public Builder fuel(IngredientSpec fuel) {
            this.fuel = fuel;
            return this;
        }

        public Builder result(McItemStack result) {
            results.add(result);
            return this;
        }

        public Builder byproduct(McItemStack byproduct, float chance) {
            this.byproduct = byproduct;
            this.byproductChance = chance;
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

        public Builder combined(boolean combined) {
            this.combined = combined;
            return this;
        }

        public WorkstationRecipeSpec build() {
            return new WorkstationRecipeSpec(this);
        }
    }
}
