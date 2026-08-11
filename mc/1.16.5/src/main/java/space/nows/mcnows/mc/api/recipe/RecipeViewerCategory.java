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
