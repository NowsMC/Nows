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

package space.nows.mc.api.registry;

import space.nows.mc.api.McId;

import java.util.Objects;

/** Stable Nows item registration data, translated by each Minecraft adapter. */
public final class ItemSpec {
    private final String id;
    private final int maxStackSize;
    private final boolean fireResistant;
    private final FoodSpec food;
    private final ToolSpec tool;
    private final ArmorSpec armor;
    private final ItemRarity rarity;
    private final int durability;

    private ItemSpec(Builder builder) {
        this.id = requireId(builder.id);
        this.maxStackSize = builder.maxStackSize;
        this.fireResistant = builder.fireResistant;
        this.food = builder.food;
        this.tool = builder.tool;
        this.armor = builder.armor;
        this.rarity = builder.rarity;
        this.durability = builder.durability;
        if (maxStackSize < 1 || maxStackSize > 99) {
            throw new IllegalArgumentException("maxStackSize must be between 1 and 99");
        }
        if (durability < 0) {
            throw new IllegalArgumentException("durability must be >= 0");
        }
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public String id() {
        return id;
    }

    public int maxStackSize() {
        return maxStackSize;
    }

    public boolean fireResistant() {
        return fireResistant;
    }

    public FoodSpec food() {
        return food;
    }

    public ToolSpec tool() {
        return tool;
    }

    public ArmorSpec armor() {
        return armor;
    }

    public ItemRarity rarity() {
        return rarity;
    }

    public int durability() {
        return durability;
    }

    public static String requireId(String id) {
        Objects.requireNonNull(id, "id");
        return McId.requireId(id);
    }

    public static final class Builder {
        private final String id;
        private int maxStackSize = 64;
        private boolean fireResistant;
        private FoodSpec food;
        private ToolSpec tool;
        private ArmorSpec armor;
        private ItemRarity rarity;
        private int durability;

        private Builder(String id) {
            this.id = id;
        }

        public Builder maxStackSize(int maxStackSize) {
            this.maxStackSize = maxStackSize;
            return this;
        }

        public Builder fireResistant() {
            this.fireResistant = true;
            return this;
        }

        public Builder food(FoodSpec food) {
            this.food = food;
            return this;
        }

        public Builder tool(ToolSpec tool) {
            this.tool = tool;
            return this;
        }

        public Builder armor(ArmorSpec armor) {
            this.armor = armor;
            return this;
        }

        public Builder rarity(ItemRarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public Builder durability(int durability) {
            this.durability = durability;
            return this;
        }

        public ItemSpec build() {
            return new ItemSpec(this);
        }
    }
}
