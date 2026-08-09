package space.nows.mcnows.mc.api.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/** Version-backed helpers for common Minecraft registries. */
public interface NowsRegistryApi {
    Item registerItem(String id);

    Item registerItem(String id, Function<Item.Properties, Item.Properties> configure);

    Item registerCustomItem(String id, Function<Item.Properties, ? extends Item> factory);

    Block registerBlock(String id);

    Block registerBlock(String id, Function<BlockBehaviour.Properties, BlockBehaviour.Properties> configure);

    Block registerCustomBlock(String id, Function<BlockBehaviour.Properties, ? extends Block> factory);

    BlockItem registerBlockItem(String id, Block block);

    BlockItem registerBlockItem(String id, Block block, Function<Item.Properties, Item.Properties> configure);

    NowsBlockEntry registerBlockWithItem(String id);

    NowsBlockEntry registerBlockWithItem(
            String id,
            Function<BlockBehaviour.Properties, BlockBehaviour.Properties> blockConfigure,
            Function<Item.Properties, Item.Properties> itemConfigure
    );

    CreativeModeTab registerCreativeTab(
            String id,
            Component title,
            Supplier<ItemStack> icon,
            CreativeModeTab.DisplayItemsGenerator displayItems
    );

    Optional<Item> item(String id);

    Optional<Block> block(String id);

    Optional<CreativeModeTab> creativeTab(String id);
}
