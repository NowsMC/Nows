package space.nows.mcnows.mc.internal.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import space.nows.mcnows.mc.api.registry.BlockLogic;
import space.nows.mcnows.mc.api.registry.BlockEntry;
import space.nows.mcnows.mc.api.registry.ItemLogic;
import space.nows.mcnows.mc.api.registry.RegistryApi;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class RegistryApiImpl implements RegistryApi {
    private int nextTabColumn;

    @Override
    public ResourceLocation id(String id) {
        return location(id);
    }

    @Override
    public <T> ResourceKey<T> key(ResourceKey<? extends Registry<T>> registry, String id) {
        return ResourceKey.create(registry, location(id));
    }

    @Override
    public <T> TagKey<T> tag(ResourceKey<? extends Registry<T>> registry, String id) {
        return TagKey.create(registry, location(id));
    }

    @Override
    public <V, T extends V> T register(Registry<V> registry, String id, T value) {
        return Registry.register(registry, location(id), value);
    }

    @Override
    public Item registerItem(String id) {
        return registerItem(id, Function.identity());
    }

    @Override
    public Item registerItem(String id, ItemLogic logic) {
        return registerItem(id, Function.identity(), logic);
    }

    @Override
    public Item registerItem(String id, Function<Item.Properties, Item.Properties> configure) {
        return registerCustomItem(id, properties -> new Item(apply(configure, properties)));
    }

    @Override
    public Item registerItem(String id, Function<Item.Properties, Item.Properties> configure, ItemLogic logic) {
        return registerCustomItem(id, properties -> new LogicItem(apply(configure, properties), logic));
    }

    @Override
    public Item registerCustomItem(String id, Function<Item.Properties, ? extends Item> factory) {
        return register(BuiltInRegistries.ITEM, id, factory.apply(new Item.Properties()));
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
    public Item registerSword(String id, Tier tier, int attackDamage, float attackSpeed) {
        return registerSword(id, tier, attackDamage, attackSpeed, Function.identity());
    }

    @Override
    public Item registerSword(
            String id,
            Tier tier,
            int attackDamage,
            float attackSpeed,
            Function<Item.Properties, Item.Properties> configure
    ) {
        return registerCustomItem(id, properties ->
                new SwordItem(tier, attackDamage, attackSpeed, apply(configure, properties)));
    }

    @Override
    public Item registerArmor(String id, ArmorMaterial material, ArmorItem.Type type) {
        return registerArmor(id, material, type, Function.identity());
    }

    @Override
    public Item registerArmor(
            String id,
            ArmorMaterial material,
            ArmorItem.Type type,
            Function<Item.Properties, Item.Properties> configure
    ) {
        return registerCustomItem(id, properties -> new ArmorItem(material, type, apply(configure, properties)));
    }

    @Override
    public Block registerBlock(String id) {
        return registerBlock(id, Function.identity());
    }

    @Override
    public Block registerBlock(String id, BlockLogic logic) {
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
            BlockLogic logic
    ) {
        return registerCustomBlock(id, properties -> new LogicBlock(apply(configure, properties), logic));
    }

    @Override
    public Block registerCustomBlock(String id, Function<BlockBehaviour.Properties, ? extends Block> factory) {
        return register(BuiltInRegistries.BLOCK, id, factory.apply(BlockBehaviour.Properties.of()));
    }

    @Override
    public BlockItem registerBlockItem(String id, Block block) {
        return registerBlockItem(id, block, Function.identity());
    }

    @Override
    public BlockItem registerBlockItem(String id, Block block, Function<Item.Properties, Item.Properties> configure) {
        return register(BuiltInRegistries.ITEM, id, new BlockItem(block, apply(configure, new Item.Properties())));
    }

    @Override
    public BlockEntry registerBlockWithItem(String id) {
        return registerBlockWithItem(id, Function.identity(), Function.identity());
    }

    @Override
    public BlockEntry registerBlockWithItem(
            String id,
            Function<BlockBehaviour.Properties, BlockBehaviour.Properties> blockConfigure,
            Function<Item.Properties, Item.Properties> itemConfigure
    ) {
        Block block = registerBlock(id, blockConfigure);
        return new BlockEntry(block, registerBlockItem(id, block, itemConfigure));
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
