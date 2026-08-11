package space.nows.mcnows.mc.api.registry;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.UseOnContext;

/** Common item behavior hooks for simple custom items. */
public interface ItemLogic {
    default InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    default void destroyed(ItemEntity entity) {
    }
}
