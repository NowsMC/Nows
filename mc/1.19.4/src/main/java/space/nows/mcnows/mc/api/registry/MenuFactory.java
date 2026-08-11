package space.nows.mcnows.mc.api.registry;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

@FunctionalInterface
public interface MenuFactory<T extends AbstractContainerMenu> {
    T create(int syncId, Inventory inventory);
}
