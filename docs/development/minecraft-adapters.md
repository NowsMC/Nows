# Minecraft Adapter APIs

`mc/<version>` exposes small Minecraft-facing API helpers for the selected game version. These helpers are meant to reduce common porting work, not hide every Minecraft class.

Mods can use the same Nows API class names across supported versions while each adapter handles common version details:

```java
RegistryApi registries = MinecraftApi.registries(context);
TextApi text = MinecraftApi.text(context);
NbtApi nbt = MinecraftApi.nbt(context);

Item raw = registries.register(BuiltInRegistries.ITEM, "my_mod:raw_widget", new Item(new Item.Properties()));
Item item = registries.registerItem("my_mod:widget", props -> props.stacksTo(16));
Item food = registries.registerFood("my_mod:berry", new FoodProperties.Builder()
        .nutrition(4)
        .saturationModifier(0.3F)
        .build());
Item sword = registries.registerSword("my_mod:blade", ToolMaterial.IRON, 3.0F, -2.4F);
Item helmet = registries.registerArmor("my_mod:helmet", ArmorMaterials.IRON, ArmorType.HELMET);
BlockEntry block = registries.registerBlockWithItem(
        "my_mod:machine",
        props -> props.strength(2.0F, 6.0F),
        props -> props);
```

## Content Mods

For content-heavy mods, the registry adapter also covers the common objects that usually differ between Fabric, Forge and Minecraft versions:

```java
RecipeType<MyRecipe> type = registries.registerRecipeType("my_mod:oven_baking");
RecipeSerializer<MyRecipe> serializer = registries.registerRecipeSerializer(
        "my_mod:oven_baking",
        MyRecipeSerializer.create());

MenuType<MyMenu> menu = registries.registerMenu("my_mod:oven", MyMenu::new);
BlockEntityType<MyBlockEntity> blockEntity = registries.registerBlockEntity(
        "my_mod:oven",
        MyBlockEntity::new,
        ovenBlock);

SoundEvent sound = registries.registerVariableRangeSound("my_mod:frying");
```

These helpers are aimed at the boring registration layer that changes shape across loaders and Minecraft versions:

- `registerRecipeType` creates the named recipe type and gives it a stable string id for recipe lookup and debugging.
- `registerRecipeSerializer` registers your custom serializer object without making the mod repeat the raw registry call in every target.
- `registerMenu` registers a simple two-argument `AbstractContainerMenu` factory, the common pattern for inventory/container menus.
- `registerBlockEntity` registers a block entity type from a Nows `BlockEntityFactory`, so mods do not need to depend on Minecraft's version-specific builder or supplier names.
- `registerVariableRangeSound` registers a regular variable-range `SoundEvent`, matching the usual custom sound event pattern.

## Item And Food Mods

For item-only or food-heavy mods, keep the mod data in your own constants and call the item helpers in a loop. This is the easiest path for mods that mainly differ by food values, stack sizes, creative tabs or generated assets:

```java
Map<String, Item> foods = new LinkedHashMap<>();

for (FoodEntry entry : FoodEntries.ALL) {
    Item item = registries.registerFood(
            "my_mod:" + entry.id(),
            new FoodProperties.Builder()
                    .nutrition(entry.nutrition())
                    .saturationModifier(entry.saturation())
                    .build(),
            props -> props.stacksTo(entry.maxStackSize()));
    foods.put(entry.id(), item);
}

registries.registerCreativeTab(
        "my_mod:foods",
        text.translatable("itemGroup.my_mod.foods"),
        () -> new ItemStack(foods.get("complete_breakfast")),
        (parameters, output) -> foods.values().forEach(output::accept));
```

## Machines And Workstations

For machine or workstation mods, keep the machine behavior in normal Minecraft subclasses and use Nows only for the stable registration boundary. A cooking block entity can still own its inventory, ticking, recipe lookup, save/load and experience logic directly. Nows only removes the repeated registry wiring:

```java
Block ovenBlock = registries.registerCustomBlock("my_mod:oven",
        props -> new OvenBlock(props.strength(3.5F)));
BlockItem ovenItem = registries.registerBlockItem("my_mod:oven", ovenBlock);

RecipeType<OvenRecipe> ovenRecipes = registries.registerRecipeType("my_mod:oven");
RecipeSerializer<OvenRecipe> ovenSerializer = registries.registerRecipeSerializer(
        "my_mod:oven",
        OvenRecipeSerializer.create());

BlockEntityType<OvenBlockEntity> ovenEntity = registries.registerBlockEntity(
        "my_mod:oven",
        OvenBlockEntity::new,
        ovenBlock);
MenuType<OvenMenu> ovenMenu = registries.registerMenu("my_mod:oven", OvenMenu::new);
```

Mods with simple facing machine blocks can reuse Nows base block classes instead of copying the same state boilerplate across versions:

```java
import space.nows.mcnows.mc.api.registry.block.HorizontalBlock;
import space.nows.mcnows.mc.api.registry.block.HorizontalLitBlock;

Block pan = registries.registerCustomBlock("my_mod:pan",
        props -> new HorizontalBlock(props.strength(1.5F)));
Block stove = registries.registerCustomBlock("my_mod:stove",
        props -> new HorizontalLitBlock(props.strength(3.5F).lightLevel(state ->
                state.getValue(HorizontalLitBlock.LIT) ? 13 : 0)));
```

`HorizontalBlock` adds the `HORIZONTAL_FACING` property, placement direction, rotation and mirror behavior. `HorizontalLitBlock` adds the same facing behavior plus Minecraft's `LIT` property. They are useful for simple props, stoves, pans, ovens and other workstation blocks. If a block has custom voxel shapes, waterlogging, redstone behavior, menu opening, ticking or entity interaction, subclass one of these or use `registerCustomBlock` with your own Minecraft block class.

## Lookup And Simple Logic

Existing registry entries can be queried with Optional-returning methods or fail-fast getters:

```java
var widgetId = registries.resourceLocation("my_mod:widget");
ResourceKey<Item> widgetKey = registries.itemKey("my_mod:widget");
TagKey<Block> machineBlocks = registries.blockTag("my_mod:machines");
registries.item("minecraft:diamond").ifPresent(stackItem -> {});
Item diamond = registries.getItem("minecraft:diamond");
Block stone = registries.getBlock("minecraft:stone");
```

Use `registries.id(...)` or `registries.resourceLocation(...)` for the selected adapter's real resource-id type. Newer adapters return `Identifier`; older adapters return `ResourceLocation`. `registries.key(...)`, `itemKey(...)`, `blockKey(...)`, `tag(...)`, `itemTag(...)` and `blockTag(...)` cover the common registry key/tag cases when the target Minecraft version exposes those classes.

Text helpers cover the common literal, translation and keybind component constructors without making mods remember when Minecraft renamed those factories:

```java
Component title = text.translatable("screen.my_mod.settings");
Component help = text.literal("Hold ").append(text.keybind("key.sneak"));
```

NBT helpers cover common compound/list reads and writes with explicit fallbacks. The returned values are still Minecraft's real NBT classes, so mods can drop down to direct APIs when needed:

```java
CompoundTag data = nbt.compound();
nbt.putString(data, "owner", "my_mod");
nbt.putInt(data, "heat", 7);

CompoundTag machine = nbt.putCompound(data, "machine");
nbt.putBoolean(machine, "active", true);

ListTag history = nbt.putList(data, "history");
nbt.addString(history, "created");
nbt.addCompound(history, nbt.copy(machine));

int heat = nbt.getInt(data, "heat", 0);
boolean active = nbt.getBoolean(nbt.getCompound(data, "machine"), "active", false);
String firstHistoryEntry = nbt.getString(nbt.getList(data, "history"), 0, "");
```

This layer intentionally avoids item-stack custom data for now. Minecraft 1.20.5+ moved much of that surface toward data components, so stack data helpers should be designed separately.

For simple behavior, mods can attach small logic hooks:

```java
registries.registerItem("my_mod:wrench", props -> props.stacksTo(1), new ItemLogic() {
    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.SUCCESS;
    }
});

registries.registerBlock("my_mod:speed_plate", props -> props.strength(1.0F), new BlockLogic() {
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.4D, 1.0D, 1.4D));
    }
});
```

For custom behavior beyond the basic layer, use Minecraft's API directly. `registerCustomItem` and `registerCustomBlock` accept your own `Item`/`Block` subclasses.
