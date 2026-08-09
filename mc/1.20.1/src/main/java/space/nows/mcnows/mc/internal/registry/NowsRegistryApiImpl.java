package space.nows.mcnows.mc.internal.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import space.nows.mcnows.mc.api.registry.NowsBlockEntry;
import space.nows.mcnows.mc.api.registry.NowsRegistryApi;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class NowsRegistryApiImpl implements NowsRegistryApi {
    private int nextTabColumn;

    @Override
    public Item registerItem(String id) {
        return registerItem(id, Function.identity());
    }

    @Override
    public Item registerItem(String id, Function<Item.Properties, Item.Properties> configure) {
        return registerCustomItem(id, properties -> new Item(apply(configure, properties)));
    }

    @Override
    public Item registerCustomItem(String id, Function<Item.Properties, ? extends Item> factory) {
        return Registry.register(BuiltInRegistries.ITEM, location(id), factory.apply(new Item.Properties()));
    }

    @Override
    public Block registerBlock(String id) {
        return registerBlock(id, Function.identity());
    }

    @Override
    public Block registerBlock(String id, Function<BlockBehaviour.Properties, BlockBehaviour.Properties> configure) {
        return registerCustomBlock(id, properties -> new Block(apply(configure, properties)));
    }

    @Override
    public Block registerCustomBlock(String id, Function<BlockBehaviour.Properties, ? extends Block> factory) {
        return Registry.register(BuiltInRegistries.BLOCK, location(id),
                factory.apply(BlockBehaviour.Properties.of()));
    }

    @Override
    public BlockItem registerBlockItem(String id, Block block) {
        return registerBlockItem(id, block, Function.identity());
    }

    @Override
    public BlockItem registerBlockItem(String id, Block block, Function<Item.Properties, Item.Properties> configure) {
        return Registry.register(BuiltInRegistries.ITEM, location(id),
                new BlockItem(block, apply(configure, new Item.Properties())));
    }

    @Override
    public NowsBlockEntry registerBlockWithItem(String id) {
        return registerBlockWithItem(id, Function.identity(), Function.identity());
    }

    @Override
    public NowsBlockEntry registerBlockWithItem(
            String id,
            Function<BlockBehaviour.Properties, BlockBehaviour.Properties> blockConfigure,
            Function<Item.Properties, Item.Properties> itemConfigure
    ) {
        Block block = registerBlock(id, blockConfigure);
        return new NowsBlockEntry(block, registerBlockItem(id, block, itemConfigure));
    }

    @Override
    public CreativeModeTab registerCreativeTab(
            String id,
            Component title,
            Supplier<ItemStack> icon,
            CreativeModeTab.DisplayItemsGenerator displayItems
    ) {
        CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, nextTabColumn++)
                .title(title)
                .icon(icon)
                .displayItems(displayItems)
                .build();
        return Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, location(id), tab);
    }

    @Override
    public Optional<Item> item(String id) {
        return BuiltInRegistries.ITEM.getOptional(location(id));
    }

    @Override
    public Optional<Block> block(String id) {
        return BuiltInRegistries.BLOCK.getOptional(location(id));
    }

    @Override
    public Optional<CreativeModeTab> creativeTab(String id) {
        return BuiltInRegistries.CREATIVE_MODE_TAB.getOptional(location(id));
    }

    private static ResourceLocation location(String id) {
        return new ResourceLocation(id);
    }

    private static <T> T apply(Function<T, T> configure, T value) {
        T configured = configure.apply(value);
        return configured == null ? value : configured;
    }
}
