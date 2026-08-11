package space.nows.mcnows.mc.internal.registry;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import space.nows.mcnows.mc.api.registry.ItemLogic;

final class LogicItem extends Item {
    private final ItemLogic logic;

    LogicItem(Properties properties, ItemLogic logic) {
        super(properties);
        this.logic = logic;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return logic.useOn(context);
    }

}
