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

package space.nows.mc.internal.recipe;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import space.nows.mc.api.recipe.RecipeViewerApi;
import space.nows.mc.api.recipe.RecipeViewerCategory;
import space.nows.mc.api.recipe.RecipeViewerLayoutFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class RecipeViewerApiImpl implements RecipeViewerApi {
    private final Map<String, RecipeViewerCategory<?>> categories = new LinkedHashMap<>();
    private final Map<String, List<ItemStack>> catalysts = new LinkedHashMap<>();
    private final Map<String, List<MenuType<?>>> transfers = new LinkedHashMap<>();

    @Override
    public synchronized <T> RecipeViewerCategory<T> registerCategory(
            String id,
            Class<T> recipeClass,
            Component title,
            ItemStack icon,
            RecipeViewerLayoutFactory<T> layoutFactory
    ) {
        RecipeViewerCategory<T> category = new RecipeViewerCategory<>(id, recipeClass, title, icon, layoutFactory);
        if (categories.putIfAbsent(id, category) != null) {
            throw new IllegalStateException("Recipe viewer category already registered: " + id);
        }
        return category;
    }

    @Override
    public synchronized Optional<RecipeViewerCategory<?>> category(String id) {
        return Optional.ofNullable(categories.get(id));
    }

    @Override
    public synchronized void registerCatalyst(String categoryId, ItemStack stack) {
        requireCategory(categoryId);
        catalysts.computeIfAbsent(categoryId, ignored -> new ArrayList<>()).add(stack.copy());
    }

    @Override
    public synchronized void registerRecipeTransfer(String categoryId, MenuType<?> menuType) {
        requireCategory(categoryId);
        transfers.computeIfAbsent(categoryId, ignored -> new ArrayList<>()).add(menuType);
    }

    @Override
    public synchronized List<RecipeViewerCategory<?>> categories() {
        return List.copyOf(categories.values());
    }

    @Override
    public synchronized List<ItemStack> catalysts(String categoryId) {
        return catalysts.getOrDefault(categoryId, List.of()).stream().map(ItemStack::copy).toList();
    }

    @Override
    public synchronized List<MenuType<?>> recipeTransfers(String categoryId) {
        return List.copyOf(transfers.getOrDefault(categoryId, List.of()));
    }

    private void requireCategory(String categoryId) {
        if (!categories.containsKey(categoryId)) {
            throw new IllegalArgumentException("Recipe viewer category is not registered: " + categoryId);
        }
    }
}
