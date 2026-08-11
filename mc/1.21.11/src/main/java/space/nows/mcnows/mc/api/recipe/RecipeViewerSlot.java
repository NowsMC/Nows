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
