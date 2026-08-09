package space.nows.mcnows.mc.api.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/** Block plus its item form registered under the same id. */
public record NowsBlockEntry(Block block, BlockItem item) {}
