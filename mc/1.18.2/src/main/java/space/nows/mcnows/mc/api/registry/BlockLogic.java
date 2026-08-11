package space.nows.mcnows.mc.api.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/** Common block behavior hooks for simple custom blocks. */
public interface BlockLogic {
    default void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
    }

    default void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
    }
}
