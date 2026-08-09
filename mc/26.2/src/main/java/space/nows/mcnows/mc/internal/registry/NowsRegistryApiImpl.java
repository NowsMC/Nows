package space.nows.mcnows.mc.internal.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import space.nows.mcnows.mc.api.registry.NowsBlockLogic;
import space.nows.mcnows.mc.api.registry.NowsBlockEntry;
import space.nows.mcnows.mc.api.registry.NowsItemLogic;
import space.nows.mcnows.mc.api.registry.NowsRegistryApi;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class NowsRegistryApiImpl implements NowsRegistryApi {
    private int nextTabColumn;

    @Override
    public Identifier id(String id) {
        return identifier(id);
    }

    @Override
    public <T> ResourceKey<T> key(ResourceKey<? extends Registry<T>> registry, String id) {
        return ResourceKey.create(registry, identifier(id));
    }

    @Override
    public <T> TagKey<T> tag(ResourceKey<? extends Registry<T>> registry, String id) {
        return TagKey.create(registry, identifier(id));
    }

    @Override
    public <V, T extends V> T register(Registry<V> registry, String id, T value) {
        return Registry.register(registry, identifier(id), value);
    }

    @Override
    public Item registerItem(String id) {
        return registerItem(id, Function.identity());
    }

    @Override
    public Item registerItem(String id, NowsItemLogic logic) {
        return registerItem(id, Function.identity(), logic);
    }

    @Override
    public Item registerItem(String id, Function<Item.Properties, Item.Properties> configure) {
        return registerCustomItem(id, properties -> new Item(apply(configure, properties)));
    }

    @Override
    public Item registerItem(String id, Function<Item.Properties, Item.Properties> configure, NowsItemLogic logic) {
        return registerCustomItem(id, properties -> new NowsLogicItem(apply(configure, properties), logic));
    }

    @Override
    public Item registerCustomItem(String id, Function<Item.Properties, ? extends Item> factory) {
        Identifier identifier = identifier(id);
        Item.Properties properties = new Item.Properties().setId(ResourceKey.create(Registries.ITEM, identifier));
        return register(BuiltInRegistries.ITEM, id, factory.apply(properties));
    }

    @Override
    public Item registerFood(String id, FoodProperties food) {
        return registerFood(id, food, Function.identity());
    }

    @Override
    public Item registerFood(String id, FoodProperties food, Function<Item.Properties, Item.Properties> configure) {
        return registerItem(id, properties -> apply(configure, properties.food(food)));
    }

    @Override
    public Item registerSword(String id, ToolMaterial material, float attackDamage, float attackSpeed) {
        return registerSword(id, material, attackDamage, attackSpeed, Function.identity());
    }

    @Override
    public Item registerSword(
            String id,
            ToolMaterial material,
            float attackDamage,
            float attackSpeed,
            Function<Item.Properties, Item.Properties> configure
    ) {
        return registerItem(id, properties -> apply(configure, properties.sword(material, attackDamage, attackSpeed)));
    }

    @Override
    public Item registerArmor(String id, ArmorMaterial material, ArmorType type) {
        return registerArmor(id, material, type, Function.identity());
    }

    @Override
    public Item registerArmor(
            String id,
            ArmorMaterial material,
            ArmorType type,
            Function<Item.Properties, Item.Properties> configure
    ) {
        return registerItem(id, properties -> apply(configure, properties.humanoidArmor(material, type)));
    }

    @Override
    public Block registerBlock(String id) {
        return registerBlock(id, Function.identity());
    }

    @Override
    public Block registerBlock(String id, NowsBlockLogic logic) {
        return registerBlock(id, Function.identity(), logic);
    }

    @Override
    public Block registerBlock(String id, Function<BlockBehaviour.Properties, BlockBehaviour.Properties> configure) {
        return registerCustomBlock(id, properties -> new Block(apply(configure, properties)));
    }

    @Override
    public Block registerBlock(
            String id,
            Function<BlockBehaviour.Properties, BlockBehaviour.Properties> configure,
            NowsBlockLogic logic
    ) {
        return registerCustomBlock(id, properties -> new NowsLogicBlock(apply(configure, properties), logic));
    }

    @Override
    public Block registerCustomBlock(String id, Function<BlockBehaviour.Properties, ? extends Block> factory) {
        Identifier identifier = identifier(id);
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, identifier));
        return register(BuiltInRegistries.BLOCK, id, factory.apply(properties));
    }

    @Override
    public BlockItem registerBlockItem(String id, Block block) {
        return registerBlockItem(id, block, Function.identity());
    }

    @Override
    public BlockItem registerBlockItem(String id, Block block, Function<Item.Properties, Item.Properties> configure) {
        Identifier identifier = identifier(id);
        Item.Properties properties = new Item.Properties().setId(ResourceKey.create(Registries.ITEM, identifier));
        return register(BuiltInRegistries.ITEM, id, new BlockItem(block, apply(configure, properties)));
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
        return register(BuiltInRegistries.CREATIVE_MODE_TAB, id, tab);
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
