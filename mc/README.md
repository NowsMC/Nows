# Minecraft version adapters

This directory owns version-specific code that talks directly to Minecraft APIs.

Use `mc/<minecraft-version>/` for API surfaces that depend on the exact Minecraft version, such as block registration, client UI/screen helpers, mod-menu hooks or future game integration points. Keep those direct `net.minecraft.*` references out of `core/`, `runtime/` and generic integrations.

Each version directory can contain:

- Java sources for the version-specific adapter artifact, for example `nows-mc-26.2`.
- Public API classes with stable Nows names, such as registry/datapack helpers, backed by that version's `net.minecraft.*` classes.
- `nows-minecraft.properties`, packaged by the generic `minecraft` module into `META-INF/nows/mc/`, for small launch-policy facts.

Prefer stable Nows-owned inputs in `space.nows.mc.api` when Minecraft changes class names, enum names, packages or constructor/property requirements between versions. For example, `ItemSpec`, `BlockSpec` and `BlockMaterial` describe basic item/block registration once, while each `mc/<version>/internal` adapter maps those specs onto that version's item properties, block properties, material model and registry rules.

Common mod-facing and generator-facing code should prefer Nows stable wrapper/spec types before reaching for raw Minecraft classes:

- Identity and geometry: `McId`, `McVec3`, `McBlockPos`, `McDirection`, `McWorldSnapshot` and `McEntitySnapshot`.
- Items and blocks: `McItemStack`, `ItemSpec`, `FoodSpec`, `ToolSpec`, `ArmorSpec`, `BlockSpec`, `BlockSound`, `MapColor`, `LightSpec`, `BlockRenderType` and `BlockShapeSpec`.
- Data, tags and recipes: `NbtCompound`, `NbtList`, `NbtValue`, `McDataComponent`, `McDataPatch`, `TagSpec`, `IngredientSpec`, `RecipeSpec` and `WorkstationRecipeSpec`.
- Containers and UI: `LayoutSpec`, `SlotSpec`, `DataSlotSpec`, `ScreenSpec` and `ScreenProgressSpec`.
- Commands and events: `CommandSpec`, `CommandArgumentSpec`, `CommandExecutionContext`, `ClientTickContext`, `ServerTickContext`, `LevelTickContext`, `PlayerEventContext`, `BlockEventContext` and `EntityEventContext`.

Use the `Mc` prefix for stable Nows-owned value or snapshot types that model Minecraft concepts directly and may otherwise collide with native or third-party names, such as `McText`, `McItemStack`, `McId` and `McVec3`. Do not use `Mc` just because a class lives in the Minecraft adapter area. Services, bridges, helpers and conversion utilities should use names that describe the role instead, such as `NativeTextBridge` or `NativeItemStackBridge`.

Raw Minecraft types can still appear in version adapter APIs for internal escape hatches, but generated code and examples should use the stable wrappers where an overload exists.

Only one `nows-mc-<minecraft-version>` artifact should be installed for a launcher profile.

Runtime installs `space.nows.mc.internal.MinecraftIntegration` when it is present. Mods should normally reach the services through `MinecraftApi.registries(context)` and `MinecraftApi.dataPacks(context)` instead of depending on runtime reflection details.

## Loading overlay flow

Keep loading state in `space.nows.platform.core.loading.NowsLoadingState` and let runtime report phases there. Each Minecraft adapter should hook that version's closest loading/splash screen, render `ClientHooks.loadingSnapshot()` with the native drawing API, and register the hook in the version's client mixin config. This same flow should hold for older adapters such as 1.12.2, even when the native hook is not called `LoadingOverlay`.

Minecraft versions that do not ship Mojang-named client jars, such as 1.20.1, need a remapped `.nows/minecraft/<version>/client-dev.jar` before their adapter module can compile.

Do not add `net.minecraft.` to parent-first classloading. Minecraft classes belong to the game classloader and should come from the selected vanilla client jar/inherited Launcher profile.
