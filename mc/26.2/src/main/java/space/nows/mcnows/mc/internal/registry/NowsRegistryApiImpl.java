package space.nows.mcnows.mc.internal.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
        Identifier identifier = identifier(id);
        Item.Properties properties = new Item.Properties().setId(ResourceKey.create(Registries.ITEM, identifier));
        return Registry.register(BuiltInRegistries.ITEM, identifier, factory.apply(properties));
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
        Identifier identifier = identifier(id);
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, identifier));
        return Registry.register(BuiltInRegistries.BLOCK, identifier, factory.apply(properties));
    }

    @Override
    public BlockItem registerBlockItem(String id, Block block) {
        return registerBlockItem(id, block, Function.identity());
    }

    @Override
    public BlockItem registerBlockItem(String id, Block block, Function<Item.Properties, Item.Properties> configure) {
        Identifier identifier = identifier(id);
        Item.Properties properties = new Item.Properties().setId(ResourceKey.create(Registries.ITEM, identifier));
        return Registry.register(BuiltInRegistries.ITEM, identifier, new BlockItem(block, apply(configure, properties)));
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
        return Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, identifier(id), tab);
    }

    @Override
    public Optional<Item> item(String id) {
        return BuiltInRegistries.ITEM.getOptional(identifier(id));
    }

    @Override
    public Optional<Block> block(String id) {
        return BuiltInRegistries.BLOCK.getOptional(identifier(id));
    }

    @Override
    public Optional<CreativeModeTab> creativeTab(String id) {
        return BuiltInRegistries.CREATIVE_MODE_TAB.getOptional(identifier(id));
    }

    private static Identifier identifier(String id) {
        return Identifier.parse(id);
    }

    private static <T> T apply(Function<T, T> configure, T value) {
        T configured = configure.apply(value);
        return configured == null ? value : configured;
    }
}
