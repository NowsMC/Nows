package space.nows.mcnows.mc.api.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.core.Registry;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/** Version-backed helpers for common Minecraft registries. */
public interface NowsRegistryApi {
    <V, T extends V> T register(Registry<V> registry, String id, T value);

    Item registerItem(String id);

    Item registerItem(String id, NowsItemLogic logic);

    Item registerItem(String id, Function<Item.Properties, Item.Properties> configure);

    Item registerItem(String id, Function<Item.Properties, Item.Properties> configure, NowsItemLogic logic);

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

    Block registerBlock(String id, NowsBlockLogic logic);

    Block registerBlock(String id, Function<BlockBehaviour.Properties, BlockBehaviour.Properties> configure);

    Block registerBlock(String id, Function<BlockBehaviour.Properties, BlockBehaviour.Properties> configure, NowsBlockLogic logic);

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
