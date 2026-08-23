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

import java.util.List;
import java.util.Objects;

/** One positioned recipe-display slot. Ingredient is used for JEI-style cycling inputs. */
public record RecipeViewerSlot(
        RecipeViewerRole role,
        int x,
        int y,
        @Nullable
        Ingredient ingredient,
        List<ItemStack> stacks
) {
    public RecipeViewerSlot {
        Objects.requireNonNull(role, "role");
        stacks = stacks.stream().map(ItemStack::copy).toList();
    }
}
