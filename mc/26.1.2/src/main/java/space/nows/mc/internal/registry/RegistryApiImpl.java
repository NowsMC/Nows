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

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import space.nows.mc.api.registry.BlockEntry;
import space.nows.mc.api.registry.BlockSpec;
import space.nows.mc.api.registry.BlockEntityFactory;
import space.nows.mc.api.registry.BlockLogic;
import space.nows.mc.api.registry.ItemSpec;
import space.nows.mc.api.registry.ItemStackSpec;
import space.nows.mc.api.registry.ItemLogic;
import space.nows.mc.api.registry.MenuFactory;
import space.nows.mc.api.registry.RegistryApi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public final class RegistryApiImpl implements RegistryApi {
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
    public Item registerItem(ItemSpec spec) {
        return registerItem(spec.id(), properties -> applyItemSpec(properties, spec));
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
    public Block registerBlock(BlockSpec spec) {
        return registerCustomBlock(spec.id(), properties -> new Block(applyBlockSpec(blockProperties(spec), spec)));
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
    public BlockEntry registerBlockWithItem(String id) {
        return registerBlockWithItem(id, Function.identity(), Function.identity());
    }

    @Override
    public BlockEntry registerBlockWithItem(BlockSpec spec) {
        Block block = registerBlock(spec);
        ItemSpec itemSpec = spec.item() == null ? ItemSpec.builder(spec.id()).build() : spec.item();
        return new BlockEntry(block, registerBlockItem(itemSpec.id(), block, properties -> applyItemSpec(properties, itemSpec)));
    }


    public ItemStack itemStack(ItemStackSpec spec) {
        return new ItemStack(getItem(spec.itemId()), spec.count());
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
    public <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
            String id,
            BlockEntityFactory<T> factory,
            Block... validBlocks
    ) {
        return register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, createBlockEntityType(factory, validBlocks));
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> registerMenu(
            String id,
            MenuFactory<T> factory
    ) {
        return register(BuiltInRegistries.MENU, id, createMenuType(factory));
    }

    @Override
    public <T extends Recipe<?>> RecipeType<T> registerRecipeType(String id) {
        Identifier identifier = identifier(id);
        return register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<>() {
            @Override
            public String toString() {
                return identifier.toString();
            }
        });
    }

    @Override
    public <T extends Recipe<?>> RecipeSerializer<T> registerRecipeSerializer(
            String id,
            RecipeSerializer<T> serializer
    ) {
        return register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializer);
    }

    @Override
    public SoundEvent registerVariableRangeSound(String id) {
        Identifier identifier = identifier(id);
        return register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(identifier));
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

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(
            BlockEntityFactory<T> factory,
            Block... validBlocks
    ) {
        try {
            Class<?> supplierType = Class.forName(BlockEntityType.class.getName() + "$BlockEntitySupplier");
            Object supplier = Proxy.newProxyInstance(
                    supplierType.getClassLoader(),
                    new Class<?>[] { supplierType },
                    (proxy, method, args) -> {
                        if ("create".equals(method.getName())) {
                            return factory.create(
                                    (net.minecraft.core.BlockPos) args[0],
                                    (net.minecraft.world.level.block.state.BlockState) args[1]);
                        }
                        return handleObjectMethod(proxy, method.getName(), args);
                    });
            Constructor<BlockEntityType> constructor = BlockEntityType.class
                    .getConstructor(supplierType, Set.class);
            return (BlockEntityType<T>) constructor.newInstance(
                    supplier,
                    new LinkedHashSet<>(Arrays.asList(validBlocks)));
        }
        catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to create block entity type", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends AbstractContainerMenu> MenuType<T> createMenuType(MenuFactory<T> factory) {
        try {
            Class<?> supplierType = Class.forName(MenuType.class.getName() + "$MenuSupplier");
            Object supplier = Proxy.newProxyInstance(
                    supplierType.getClassLoader(),
                    new Class<?>[] { supplierType },
                    (proxy, method, args) -> {
                        if ("create".equals(method.getName())) {
                            return factory.create(
                                    (Integer) args[0],
                                    (net.minecraft.world.entity.player.Inventory) args[1]);
                        }
                        return handleObjectMethod(proxy, method.getName(), args);
                    });
            Constructor<MenuType> constructor = MenuType.class
                    .getDeclaredConstructor(supplierType, net.minecraft.world.flag.FeatureFlagSet.class);
            constructor.setAccessible(true);
            return (MenuType<T>) constructor.newInstance(supplier, FeatureFlags.VANILLA_SET);
        }
        catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to create menu type", exception);
        }
    }

    private static Object handleObjectMethod(Object proxy, String methodName, Object[] args) {
        return switch (methodName) {
            case "toString" -> proxy.getClass().getName();
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> null;
        };
    }

    private static Item.Properties applyItemSpec(Item.Properties properties, ItemSpec spec) {
        Item.Properties configured = properties.stacksTo(spec.maxStackSize());
        if (spec.fireResistant()) {
            configured = configured.fireResistant();
        }
        return configured;
    }

    private static BlockBehaviour.Properties blockProperties(BlockSpec spec) {
        return BlockBehaviour.Properties.of();
    }

    private static BlockBehaviour.Properties applyBlockSpec(BlockBehaviour.Properties properties, BlockSpec spec) {
        BlockBehaviour.Properties configured = properties.strength(spec.destroyTime(), spec.explosionResistance());
        if (spec.requiresCorrectTool()) {
            configured = configured.requiresCorrectToolForDrops();
        }
        if (spec.noOcclusion()) {
            configured = configured.noOcclusion();
        }
        return configured;
    }

    private static <T> T apply(Function<T, T> configure, T value) {
        T configured = configure.apply(value);
        return configured == null ? value : configured;
    }
}
