package space.nows.mcnows.mc.api.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
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
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/** Version-backed helpers for common Minecraft registries. */
public interface RegistryApi {
    ResourceLocation id(String id);

    default ResourceLocation resourceLocation(String id) {
        return id(id);
    }

    <T> ResourceKey<T> key(ResourceKey<? extends Registry<T>> registry, String id);

    <T> TagKey<T> tag(ResourceKey<? extends Registry<T>> registry, String id);

    default ResourceKey<Item> itemKey(String id) {
        return key(Registries.ITEM, id);
    }

    default ResourceKey<Block> blockKey(String id) {
        return key(Registries.BLOCK, id);
    }

    default TagKey<Item> itemTag(String id) {
        return tag(Registries.ITEM, id);
    }

    default TagKey<Block> blockTag(String id) {
        return tag(Registries.BLOCK, id);
    }

    <V, T extends V> T register(Registry<V> registry, String id, T value);

    Item registerItem(String id);

    Item registerItem(ItemSpec spec);

    Item registerItem(String id, ItemLogic logic);

    Item registerItem(String id, Function<Item.Properties, Item.Properties> configure);

    Item registerItem(String id, Function<Item.Properties, Item.Properties> configure, ItemLogic logic);

    Item registerCustomItem(String id, Function<Item.Properties, ? extends Item> factory);

    Item registerFood(String id, FoodProperties food);

    Item registerFood(String id, FoodProperties food, Function<Item.Properties, Item.Properties> configure);

    Item registerSword(String id, Tier tier, int attackDamage, float attackSpeed);

    Item registerSword(
            String id,
            Tier tier,
            int attackDamage,
            float attackSpeed,
            Function<Item.Properties, Item.Properties> configure
    );

    Item registerArmor(String id, ArmorMaterial material, ArmorItem.Type type);

    Item registerArmor(
            String id,
            ArmorMaterial material,
            ArmorItem.Type type,
            Function<Item.Properties, Item.Properties> configure
    );

    Block registerBlock(String id);

    Block registerBlock(BlockSpec spec);

    Block registerBlock(String id, BlockLogic logic);

    Block registerBlock(String id, Function<BlockBehaviour.Properties, BlockBehaviour.Properties> configure);

    Block registerBlock(String id, Function<BlockBehaviour.Properties, BlockBehaviour.Properties> configure, BlockLogic logic);

    Block registerCustomBlock(String id, Function<BlockBehaviour.Properties, ? extends Block> factory);

    BlockItem registerBlockItem(String id, Block block);

    BlockItem registerBlockItem(String id, Block block, Function<Item.Properties, Item.Properties> configure);

    BlockEntry registerBlockWithItem(String id);

    BlockEntry registerBlockWithItem(BlockSpec spec);

    ItemStack itemStack(ItemStackSpec spec);


    BlockEntry registerBlockWithItem(
            String id,
            Function<BlockBehaviour.Properties, BlockBehaviour.Properties> blockConfigure,
            Function<Item.Properties, Item.Properties> itemConfigure
    );

    <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
            String id,
            BlockEntityFactory<T> factory,
            Block... validBlocks
    );

    <T extends AbstractContainerMenu> MenuType<T> registerMenu(
            String id,
            MenuFactory<T> factory
    );

    <T extends Recipe<?>> RecipeType<T> registerRecipeType(String id);

    <T extends Recipe<?>> RecipeSerializer<T> registerRecipeSerializer(
            String id,
            RecipeSerializer<T> serializer
    );

    SoundEvent registerVariableRangeSound(String id);

    CreativeModeTab registerCreativeTab(
            String id,
            Component title,
            Supplier<ItemStack> icon,
            CreativeModeTab.DisplayItemsGenerator displayItems
    );

    Optional<Item> item(String id);

    default Item getItem(String id) {
        return item(id).orElseThrow(() -> new IllegalArgumentException("Unknown Minecraft item: " + id));
    }

    Optional<Block> block(String id);

    default Block getBlock(String id) {
        return block(id).orElseThrow(() -> new IllegalArgumentException("Unknown Minecraft block: " + id));
    }

    Optional<CreativeModeTab> creativeTab(String id);

    default CreativeModeTab getCreativeTab(String id) {
        return creativeTab(id).orElseThrow(() -> new IllegalArgumentException("Unknown Minecraft creative tab: " + id));
    }
}
