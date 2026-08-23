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

package space.nows.mcnows.mc.api.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Immutable slot layout for one displayed recipe. */
public record RecipeViewerLayout(List<RecipeViewerSlot> slots) {
    public RecipeViewerLayout {
        slots = List.copyOf(slots);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<RecipeViewerSlot> slots = new ArrayList<>();

        public Builder input(int x, int y, Ingredient ingredient) {
            return slot(RecipeViewerRole.INPUT, x, y, Objects.requireNonNull(ingredient, "ingredient"), List.of());
        }

        public Builder inputStack(int x, int y, ItemStack stack) {
            return slot(RecipeViewerRole.INPUT, x, y, null, List.of(copy(stack)));
        }

        public Builder inputStacks(int x, int y, List<ItemStack> stacks) {
            return slot(RecipeViewerRole.INPUT, x, y, null, stacks);
        }

        public Builder output(int x, int y, ItemStack stack) {
            return slot(RecipeViewerRole.OUTPUT, x, y, null, List.of(copy(stack)));
        }

        public Builder outputStacks(int x, int y, List<ItemStack> stacks) {
            return slot(RecipeViewerRole.OUTPUT, x, y, null, stacks);
        }

        public Builder catalyst(int x, int y, ItemStack stack) {
            return slot(RecipeViewerRole.CATALYST, x, y, null, List.of(copy(stack)));
        }

        public Builder slot(RecipeViewerRole role, int x, int y, @Nullable Ingredient ingredient, List<ItemStack> stacks) {
            slots.add(new RecipeViewerSlot(role, x, y, ingredient, stacks));
            return this;
        }

        public RecipeViewerLayout build() {
            return new RecipeViewerLayout(slots);
        }

        private static ItemStack copy(ItemStack stack) {
            return Objects.requireNonNull(stack, "stack").copy();
        }
    }
}
