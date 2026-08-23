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

package space.nows.mc.internal.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import space.nows.mc.api.registry.BlockLogic;

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
