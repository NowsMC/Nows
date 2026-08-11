package space.nows.mcnows.mc.api.recipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

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

    Optional<RecipeViewerCategory<?>> category(String id);

    void registerCatalyst(String categoryId, ItemStack stack);

    void registerRecipeTransfer(String categoryId, MenuType<?> menuType);

    List<RecipeViewerCategory<?>> categories();

    List<ItemStack> catalysts(String categoryId);

    List<MenuType<?>> recipeTransfers(String categoryId);
}
