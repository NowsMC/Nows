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

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import space.nows.mc.api.registry.McItemStack;
import space.nows.mc.api.registry.NativeItemStackBridge;
import space.nows.mc.api.text.McText;
import space.nows.mc.api.text.NativeTextBridge;

import java.util.List;
import java.util.Optional;

/** Registry for recipe display metadata that can be bridged to JEI, REI, EMI or a built-in viewer. */
public interface RecipeViewerApi {
    <T> RecipeViewerCategory<T> registerCategory(
            String id,
            Class<T> recipeClass,
            Component title,
            ItemStack icon,
            RecipeViewerLayoutFactory<T> layoutFactory
    );

    default <T> RecipeViewerCategory<T> registerCategory(
            String id,
            Class<T> recipeClass,
            McText title,
            McItemStack icon,
            RecipeViewerLayoutFactory<T> layoutFactory
    ) {
        return registerCategory(
                id,
                recipeClass,
                NativeTextBridge.nativeComponent(title, Component.class),
                NativeItemStackBridge.nativeStack(icon, ItemStack.class),
                layoutFactory);
    }

    Optional<RecipeViewerCategory<?>> category(String id);

    void registerCatalyst(String categoryId, ItemStack stack);

    default void registerCatalyst(String categoryId, McItemStack stack) {
        registerCatalyst(categoryId, NativeItemStackBridge.nativeStack(stack, ItemStack.class));
    }

    default void registerCatalyst(String categoryId, String itemId) {
        registerCatalyst(categoryId, McItemStack.of(itemId));
    }

    void registerRecipeTransfer(String categoryId, MenuType<?> menuType);

    List<RecipeViewerCategory<?>> categories();

    List<ItemStack> catalysts(String categoryId);

    List<MenuType<?>> recipeTransfers(String categoryId);
}
