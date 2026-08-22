package space.nows.mcnows.mc.internal.registry;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import space.nows.mcnows.mc.api.registry.BlockEntry;
import space.nows.mcnows.mc.api.registry.BlockMaterial;
import space.nows.mcnows.mc.api.registry.BlockSpec;
import space.nows.mcnows.mc.api.registry.BlockEntityFactory;
import space.nows.mcnows.mc.api.registry.BlockLogic;
import space.nows.mcnows.mc.api.registry.ItemSpec;
import space.nows.mcnows.mc.api.registry.ItemStackSpec;
import space.nows.mcnows.mc.api.registry.ItemLogic;
import space.nows.mcnows.mc.api.registry.MenuFactory;
import space.nows.mcnows.mc.api.registry.RegistryApi;

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
    public ResourceLocation id(String id) {
        return location(id);
    }

    @Override
    public <T> ResourceKey<T> key(ResourceKey<? extends Registry<T>> registry, String id) {
        return ResourceKey.create(registry, location(id));
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
        return register(Registry.ITEM, id, factory.apply(new Item.Properties()));
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
    public Item registerArmor(String id, ArmorMaterial material, EquipmentSlot slot) {
        return registerArmor(id, material, slot, Function.identity());
    }

    @Override
    public Item registerArmor(
            String id,
            ArmorMaterial material,
            EquipmentSlot slot,
            Function<Item.Properties, Item.Properties> configure
    ) {
        return registerCustomItem(id, properties -> new ArmorItem(material, slot, apply(configure, properties)));
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
        return register(Registry.BLOCK, id, factory.apply(BlockBehaviour.Properties.of(Material.STONE)));
    }

    @Override
    public BlockItem registerBlockItem(String id, Block block) {
        return registerBlockItem(id, block, Function.identity());
    }

    @Override
    public BlockItem registerBlockItem(String id, Block block, Function<Item.Properties, Item.Properties> configure) {
        return register(Registry.ITEM, id, new BlockItem(block, apply(configure, new Item.Properties())));
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
        return register(Registry.BLOCK_ENTITY_TYPE, id, createBlockEntityType(factory, validBlocks));
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> registerMenu(
            String id,
            MenuFactory<T> factory
    ) {
        return register(Registry.MENU, id, createMenuType(factory));
    }

    @Override
    public <T extends Recipe<?>> RecipeType<T> registerRecipeType(String id) {
        ResourceLocation location = location(id);
        return register(Registry.RECIPE_TYPE, id, new RecipeType<>() {
            @Override
            public String toString() {
                return location.toString();
            }
        });
    }

    @Override
    public <T extends Recipe<?>> RecipeSerializer<T> registerRecipeSerializer(
            String id,
            RecipeSerializer<T> serializer
    ) {
        return register(Registry.RECIPE_SERIALIZER, id, serializer);
    }

    @Override
    public SoundEvent registerVariableRangeSound(String id) {
        ResourceLocation location = location(id);
        return register(Registry.SOUND_EVENT, id, new SoundEvent(location));
    }

    @Override
    public CreativeModeTab registerCreativeTab(
            String id,
            Component title,
            Supplier<ItemStack> icon
    ) {
        return CreativeModeTab.TAB_MISC;
    }

    @Override
    public Optional<Item> item(String id) {
        return Registry.ITEM.getOptional(location(id));
    }

    @Override
    public Optional<Block> block(String id) {
        return Registry.BLOCK.getOptional(location(id));
    }

    @Override
    public Optional<CreativeModeTab> creativeTab(String id) {
        return Optional.empty();
    }

    private static ResourceLocation location(String id) {
        return new ResourceLocation(id);
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
                    .getConstructor(supplierType, Set.class, com.mojang.datafixers.types.Type.class);
            return (BlockEntityType<T>) constructor.newInstance(
                    supplier,
                    new LinkedHashSet<>(Arrays.asList(validBlocks)),
                    null);
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
                    .getDeclaredConstructor(supplierType);
            constructor.setAccessible(true);
            return (MenuType<T>) constructor.newInstance(supplier);
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
        return BlockBehaviour.Properties.of(material(spec.material()));
    }

    private static Material material(BlockMaterial material) {
        return switch (material) {
            case AIR -> Material.AIR;
            case PLANT -> Material.PLANT;
            case DIRT -> Material.DIRT;
            case STONE -> Material.STONE;
            case WOOD -> Material.WOOD;
            case METAL -> Material.METAL;
            case GLASS -> Material.GLASS;
        };
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
