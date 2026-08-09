package space.nows.mcnows.mc.internal.registry;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import space.nows.mcnows.mc.api.registry.NowsItemLogic;

final class NowsLogicItem extends Item {
    private final NowsItemLogic logic;

    NowsLogicItem(Properties properties, NowsItemLogic logic) {
        super(properties);
        this.logic = logic;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return logic.useOn(context);
    }

    @Override
    public void onDestroyed(ItemEntity entity) {
        logic.destroyed(entity);
        super.onDestroyed(entity);
    }
}
