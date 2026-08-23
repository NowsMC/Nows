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

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/** Stable category description for mods that already have JEI-style recipe categories. */
public record RecipeViewerCategory<T>(
        String id,
        Class<T> recipeClass,
        Component title,
        ItemStack icon,
        RecipeViewerLayoutFactory<T> layoutFactory
) {
    public RecipeViewerCategory {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Recipe viewer category id must not be blank");
        }
        Objects.requireNonNull(recipeClass, "recipeClass");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(layoutFactory, "layoutFactory");
        icon = icon.copy();
    }

    public RecipeViewerLayout layout(T recipe) {
        return Objects.requireNonNull(
                layoutFactory.build(recipe, RecipeViewerLayout.builder()),
                "layoutFactory returned null");
    }
}
