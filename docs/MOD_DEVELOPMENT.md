# Mod development guide

This guide collects the mod-facing details that used to live in the root README. Nows is a portability layer, not a full replacement for Minecraft APIs. Use the Nows helpers where they make cross-version work easier, and use Minecraft's own API directly for behavior that needs full control.

## Metadata

KDL is the recommended human-facing metadata format today, but it is replaceable. `integrations/kdl` turns `nows.mod.kdl` into the generic `ModDescriptor` model owned by `core`.

```kdl
mod id="my_mod" name="My Mod" version="1.0.0" minecraft="26.2" side="client" {
    info {
        description "Short description shown to tools and companion UI."
        author "YourName"
        license "Apache-2.0"
        icon "assets/my_mod/icon.png"
    }

    links {
        homepage "https://example.com"
        sources "https://github.com/example/my-mod"
    }

    compatibility {
        requires "minecraft" version="26.2"
        depends "cloth-config" version=">=11.0.0"
        recommends "modmenu" version=">=1.0.0"
        incompatible-with "bad_mod" reason="Breaks the same screen"
    }

    load-order {
        after "cloth-config"
        before "late_mod"
    }

    properties {
        channel "stable"
    }

    runtime {
        network-channel "my_mod:main"
        listener "com.example.MyLifecycleListener"
        entrypoint "com.example.MyMod"
        transformer "com.example.MyTransformer"
        mixin "my_mod.mixins.json"
    }
}
```

Grouped KDL is the recommended style, but equivalent flat nodes remain supported. Runtime-provided ids such as `minecraft`, `nows` and `nows-loader` can be used in dependency declarations.

`side` accepts `client`, `server`, `both` or `common`. The current launcher runtime is client-side and rejects server-only mods before loading mod classes.

Mods can query loaded metadata through `NowsContext`:

```java
if (context.isModLoaded("other_mod")) {
    String name = context.requireModDescriptor("other_mod").name();
}

boolean clientRuntime = context.side() == NowsSide.CLIENT;
```

## Lifecycle events

`integrations/geb` registers a GEB instance in `NowsServices`. Mods that want it can use the small Nows facade:

```java
NowsEvents events = GebIntegration.events(context);
events.post(new MyCustomEvent());
```

Mods can declare no-argument listener classes in `nows.mod.kdl` with `listener "com.example.Listener"`. Nows registers those listeners before entrypoints run.

For Nows-owned loader lifecycle events, implement `NowsLifecycleListener`:

```java
public final class MyLifecycleListener implements NowsLifecycleListener {
    @Override
    public void onNowsEntrypointsCompleted(NowsEntrypointsCompletedEvent event) {
        int loadedEntrypoints = event.count();
    }
}
```

Lifecycle events include `NowsBootstrapReadyEvent`, `NowsEntrypointsStartingEvent`, `NowsModEntrypointStartingEvent`, `NowsModEntrypointCompletedEvent`, `NowsEntrypointsCompletedEvent` and `NowsMinecraftStartingEvent`.

A mod that does not care about GEB can still compile against `nows-core` without pulling GEB into its API.

## Network channels

`integrations/network` exposes a small networking surface over Minecraft's existing Netty stack without importing version-specific Minecraft packet classes.

Declare channels in metadata, then use `NowsNetworking` from the runtime context:

```java
NowsNetworking networking = NowsNetworking.service(context);
networking.registerHandler("my_mod:main", NetworkDirection.CLIENTBOUND, (packet, payload) -> {
    int bytes = payload.size();
    ByteBuf buffer = payload.buffer();
});
```

Payloads are backed by Netty `ByteBuf`. Nows expects Minecraft launcher/runtime libraries to provide Netty and does not bundle a second copy.

Sending goes through `NetworkTransport`, which is installed by version-specific code. Until a concrete transport is present, `send(...)` returns `false`.

## Minecraft adapter APIs

`mc/<version>` exposes small Minecraft-facing API helpers for the selected game version. These helpers are meant to reduce common porting work, not hide every Minecraft class.

Mods can use the same Nows API class names across supported versions while each adapter handles common version details:

```java
RegistryApi registries = MinecraftApi.registries(context);

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

Existing registry entries can be queried with Optional-returning methods or fail-fast getters:

```java
ResourceKey<Item> widgetKey = registries.itemKey("my_mod:widget");
TagKey<Block> machineBlocks = registries.blockTag("my_mod:machines");
registries.item("minecraft:diamond").ifPresent(stackItem -> {});
Item diamond = registries.getItem("minecraft:diamond");
Block stone = registries.getBlock("minecraft:stone");
```

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

## Data packs, commands and generated data

Datapack and resource-pack sources are exposed through the version adapter:

```java
DataPacks packs = MinecraftApi.dataPacks(context);
Path modPackDir = packs.modPackDirectory("my_mod");
packs.registerSource(PackType.SERVER_DATA, repositoryConsumer -> {
    // Create or forward Minecraft RepositorySource packs for this version.
});
```

Commands and generated JSON data are collected through the adapter as well:

```java
MinecraftApi.commands(context).register(dispatcher ->
        dispatcher.register(Commands.literal("my_mod").executes(command -> 1)));

DataGen dataGen = MinecraftApi.dataGen(context);
dataGen.writeJson(dataGen.recipePath("my_mod:widget"), Map.class, Map.of(
        "type", "minecraft:crafting_shapeless",
        "ingredients", List.of(Map.of("item", "minecraft:stone")),
        "result", Map.of("id", "my_mod:widget", "count", 1)
));
```

## Config screens

Nows includes a small config screen layer for mods that only need common settings UI instead of depending directly on Cloth Config or a loader-specific mod menu integration.

Register a config screen factory during initialization:

```java
MinecraftApi.configUi(context).register("my_mod", parent ->
        MinecraftApi.configUi(context)
                .screen(parent, Component.literal("My Mod Settings"))
                .category(Component.literal("General"))
                .booleanOption(
                        Component.literal("Enable Feature"),
                        MyConfig.enabled(),
                        true,
                        Component.literal("Turn the main feature on or off."),
                        MyConfig::setEnabled)
                .intOption(
                        Component.literal("Cooldown"),
                        MyConfig.cooldown(),
                        1000,
                        0,
                        60000,
                        Component.literal("Cooldown in milliseconds."),
                        MyConfig::setCooldown)
                .done()
                .saving(MyConfig::save)
                .build());
```

The built-in Nows mod list opens registered config screens from its Configure button. This covers the small, common Cloth Config pattern: category, boolean toggle, integer field, default/reset and save callback. More complex screens can still be custom Minecraft `Screen` classes returned by the registered factory.

## Game events

Version adapters expose lightweight game callbacks for common Fabric/Forge event patterns:

```java
MinecraftApi.events(context).clientTick(minecraft -> {
    // Poll keybinds or update client-only helpers.
});

MinecraftApi.events(context).serverTick(server -> {
    // Run server-wide maintenance.
});

MinecraftApi.events(context).serverLevelTick((server, level) -> {
    // Run per-level managers such as spell effects or delayed block changes.
});
```

This is intentionally small. Use it for simple tick-driven managers like cooldown cleanup, gradual world effects or floating projectile state. Detailed player interaction and networking still belong in the dedicated API surfaces as they grow.

## Client UI and player helpers

Client UI helpers cover common cases: opening a screen, making a simple screen, adding title-screen buttons and drawing small overlays. Raw Minecraft UI objects remain exposed for deeper behavior.

```java
Ui ui = MinecraftApi.ui(context);
ui.titleScreen().addButton(title -> title.button(
        title.centerX(98), title.height() / 4 + 120, 98, 20,
        Component.literal("My Mod"),
        button -> ui.show(ui.simpleScreen(
                Component.literal("My Mod"),
                screen -> screen.addButton(screen.centerX(120), screen.height() - 40, 120, 20,
                        Component.literal("Done"), done -> ui.close()),
                render -> render.centeredText(Component.literal("Hello from Nows UI"),
                        render.width() / 2, 40, 0xFFFFFFFF)
        ))
));

ui.titleScreen().render(render ->
        render.text("my_mod loaded", 2, render.height() - 42, 0xFFAAFFAA));
```

Client player helpers expose both nullable and fail-fast player access. On multiplayer servers, server-authoritative values may be corrected by the server:

```java
PlayerApi player = MinecraftApi.player(context);
player.current().ifPresent(local -> {
    PlayerSnapshot snapshot = player.snapshot();
    player.sendOverlayMessage(Component.literal("Hello " + snapshot.name()));
    player.setHealth(Math.min(snapshot.maxHealth(), snapshot.health() + 2.0F));
    player.setFood(20);
    player.setSelectedHotbarSlot(0);
});
```

## Config files

Basic per-mod config files live in core because they are not Minecraft-version-specific:

```java
Properties config = context.configs().loadProperties("my_mod", "client");
context.configs().saveProperties("my_mod", "client", config, "My Mod client config");
```

Runtime installs these APIs through `NowsServices` when a matching `nows-mc-<version>` adapter is present. `core` stays free of `net.minecraft.*` types.

## Mixin

`integrations/mixin` provides Nows' own `IMixinService` and `IGlobalPropertyService`. The Mixin transformer is inserted directly into `NowsClassLoader` before class definition, including synthetic class generation. Fabric Loader is not required.
