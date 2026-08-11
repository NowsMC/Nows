package space.nows.mcnows.mc.internal.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import space.nows.mcnows.mc.api.registry.BlockLogic;

final class LogicBlock extends Block {
    private final BlockLogic logic;

    LogicBlock(BlockBehaviour.Properties properties, BlockLogic logic) {
        super(properties);
        this.logic = logic;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        logic.destroy(level, pos, state);
        super.destroy(level, pos, state);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, Entity entity) {
        logic.stepOn(level, pos, level.getBlockState(pos), entity);
        super.stepOn(level, pos, entity);
    }
}
