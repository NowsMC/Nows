# Nows

Nows is an experimental Minecraft Java mod loader built around a small stable kernel and replaceable integrations.

- Website/domain: **https://nows.space**
- Java/Maven namespace: **`space.nows.mcnows`**
- Current source version: **0.4.0**
- Current target: **Minecraft Java 26.2**
- No Java agent, `premain`, or `java.lang.instrument`
- Development names: **official Mojang mappings**

## License

Nows is licensed under the Apache License, Version 2.0. Earlier private development commits may have carried MIT license text; those commits were kept in a private development repository and were not public releases of Nows. The project license for Nows source, documentation and release artifacts is Apache License 2.0 from this point forward.

## Architecture rule

`nows-core` is deliberately boring. It contains only contracts that should survive loader changes: `ModInitializer`, `ClassTransformer`, `NowsContext`, typed services, format-neutral mod descriptors/discovery and `NowsClassLoader`.

Everything likely to change is outside core:

For the exact change boundary and dependency direction, see [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).


```text
Nows/
├─ core/                         stable kernel, no external runtime stack
├─ minecraft/                    Minecraft launch/version policy
├─ integrations/
│  ├─ kdl/                       nows.mod.kdl + KDL4J
│  ├─ geb/                       GEB event bus
│  ├─ logging/                   Reactor Loggers + Async Log4j support pieces
│  ├─ network/                   Nows network channels + transport facade
│  └─ mixin/                     Nows IMixinService integration
├─ runtime/                      composes the modules; contains main()
├─ repos/
│  ├─ NowsInstaller/             internet installer
│  ├─ NowsGradlePlugin/          mappings/dev tooling/Gradle workarounds
│  ├─ NowsApiMod/                optional API/helper mod submodule
│  └─ NowsWeb/                   web/download surface submodule
└─ example-mod/
```

Normal installation is intentionally modular: the Official Launcher receives several Nows library JARs plus only the third-party libraries Nows owns. `./gradlew allJar` is an optional monolithic path that merges the Nows modules and Nows-owned runtime libraries while still excluding libraries supplied by Minecraft.

## Runtime stack

Requested components are isolated by integration:

```kotlin
implementation("foo.zaaarf.geb:processor:0.4.9")
implementation("foo.zaaarf.geb:core:0.5.4")
annotationProcessor("foo.zaaarf.geb:processor:0.4.9")
implementation("dev.kdl:kdl4j:1.0.1")
implementation("io.projectreactor:reactor-core:3.8.6")
implementation("com.lmax:disruptor:4.0.0")
implementation("org.spongepowered:mixin:0.8.7")
```

Minecraft-owned Log4j2, SLF4J, Gson, Guava and JSpecify are not bundled into the loader distribution again. See [`docs/DEPENDENCY_OWNERSHIP.md`](docs/DEPENDENCY_OWNERSHIP.md) for the delivery boundary. Reactor is forced onto the game's existing SLF4J backend, while the installed profile enables Log4j2's async context with:

```text
-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
```

Nows logs loader progress at INFO level: phase start/end, selected Minecraft policy, mod directory scan, classloader setup, built-in Mixin configs, transformers, services and entrypoints. Use `-Dnows.logging.backend=console`, `verbose-console`, `jdk` or `slf4j` to force a backend while debugging.

## KDL is replaceable metadata

The core never imports KDL4J. `integrations/kdl` implements the `ModMetadataReader` SPI and turns `nows.mod.kdl` into a generic `ModDescriptor`.

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

Mods can query loaded metadata through `NowsContext`:

```java
if (context.isModLoaded("other_mod")) {
    String name = context.requireModDescriptor("other_mod").name();
}

boolean clientRuntime = context.side() == NowsSide.CLIENT;
```

`side` is typed metadata and accepts `client`, `server` or `both`/`common`. The current launcher runtime is client-side and rejects server-only mods with a clear compatibility error before loading mod classes. Unknown future declaration names can be retained without changing core because declarations are stored by key rather than by fixed record fields. Extra root properties are exposed through `ModDescriptor.property("key")`.

Grouped KDL is the recommended human-facing style, but flat nodes remain supported for compact files and compatibility with older mods. Dependency metadata is validated before mod classes are loaded. `depends`/`requires` must be present and match the requested version, `recommends`/`suggests` are optional, `conflicts`/`breaks`/`incompatible-with` reject matching loaded/provided ids, and `load-before`/`load-after` or `before`/`after` influence deterministic entrypoint order when the target mod is present. Runtime provides virtual ids such as `minecraft`, `nows` and `nows-loader` for requirements.

## GEB without coupling core to GEB

`integrations/geb` registers the GEB instance in `NowsServices`. Mods that want it can use:

```java
NowsEvents events = GebIntegration.events(context);
events.post(new MyCustomEvent());
```

Mods can declare no-argument GEB listener classes in `nows.mod.kdl` with `listener "com.example.Listener"`. Nows registers those listeners before entrypoints run. For Nows-owned loader lifecycle events, implement `NowsLifecycleListener` and override the hooks you need:

```java
public final class MyLifecycleListener implements NowsLifecycleListener {
    @Override
    public void onNowsEntrypointsCompleted(NowsEntrypointsCompletedEvent event) {
        int loadedEntrypoints = event.count();
    }
}
```

Lifecycle events include `NowsBootstrapReadyEvent`, `NowsEntrypointsStartingEvent`, `NowsModEntrypointStartingEvent`, `NowsModEntrypointCompletedEvent`, `NowsEntrypointsCompletedEvent` and `NowsMinecraftStartingEvent`. Regular GEB `@Listen` methods are still useful for mod-owned custom events and dispatchers generated by the GEB processor.

A mod that does not care about GEB still compiles against `nows-core` without pulling GEB into its API.

## Network channels without version lock-in

`integrations/network` exposes a small Nows networking surface over Minecraft's existing Netty stack without importing version-specific Minecraft packet classes. Mods declare channels in metadata and use `NowsNetworking` from the runtime context:

```java
NowsNetworking networking = NowsNetworking.service(context);
networking.registerHandler("my_mod:main", NetworkDirection.CLIENTBOUND, (packet, payload) -> {
    int bytes = payload.size();
    ByteBuf buffer = payload.buffer();
});
```

The current runtime registers declared channels and handlers immediately. Payloads are backed by Netty `ByteBuf`; Nows depends on Netty only for compilation and expects the Minecraft launcher/runtime libraries to provide it. Sending goes through a `NetworkTransport`, which is intentionally installed by version-specific Minecraft/network code later; until then `send(...)` returns `false` instead of pretending a real connection exists.

## Minecraft API adapter

`mc/<version>` exposes a basic Minecraft-facing API layer for the selected game version. It is intentionally small: Nows helps with common registry work so simple mods are easier to port between Minecraft versions, but it does not try to replace Minecraft's own API.

Mods can use the same Nows API class names across supported versions while each adapter hides common version details such as `ResourceLocation` versus `Identifier` and newer registry `ResourceKey` setup:

```java
NowsRegistryApi registries = NowsMinecraft.registries(context);

Item raw = registries.register(BuiltInRegistries.ITEM, "my_mod:raw_widget", new Item(new Item.Properties()));
Item item = registries.registerItem("my_mod:widget", props -> props.stacksTo(16));
Item food = registries.registerFood("my_mod:berry", new FoodProperties.Builder()
        .nutrition(4)
        .saturationModifier(0.3F)
        .build());
Item sword = registries.registerSword("my_mod:blade", ToolMaterial.IRON, 3.0F, -2.4F);
Item helmet = registries.registerArmor("my_mod:helmet", ArmorMaterials.IRON, ArmorType.HELMET);
NowsBlockEntry block = registries.registerBlockWithItem(
        "my_mod:machine",
        props -> props.strength(2.0F, 6.0F),
        props -> props);

registries.registerCreativeTab(
        "my_mod:main",
        Component.translatable("itemGroup.my_mod.main"),
        () -> new ItemStack(item),
        (parameters, output) -> {
            output.accept(item);
            output.accept(food);
            output.accept(sword);
            output.accept(helmet);
            output.accept(block.item());
        });
```

`NowsServices.register(...)` is only the loader's service registry. `NowsRegistryApi.register(...)` is the raw Minecraft registry helper, similar in spirit to the basic registry calls a Forge/Fabric mod reaches for when no higher-level helper is needed.

Existing registry entries can be queried with Optional-returning methods or fail-fast getters:

```java
ResourceKey<Item> widgetKey = registries.itemKey("my_mod:widget");
TagKey<Block> machineBlocks = registries.blockTag("my_mod:machines");
registries.item("minecraft:diamond").ifPresent(stackItem -> {});
Item diamond = registries.getItem("minecraft:diamond");
Block stone = registries.getBlock("minecraft:stone");
```

For simple behavior, mods can attach small logic hooks without writing a full subclass:

```java
registries.registerItem("my_mod:wrench", props -> props.stacksTo(1), new NowsItemLogic() {
    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Run custom item behavior for this Minecraft version.
        return InteractionResult.SUCCESS;
    }
});

registries.registerBlock("my_mod:speed_plate", props -> props.strength(1.0F), new NowsBlockLogic() {
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.4D, 1.0D, 1.4D));
    }
});
```

For custom behavior beyond this basic layer, use Minecraft's API directly. `registerCustomItem` and `registerCustomBlock` accept your own `Item`/`Block` subclasses, so every version-specific override remains available without pushing all Minecraft methods into Nows' wrapper API.

Forge and Fabric take a similar shape in practice: they provide loader APIs and registry conveniences, but serious item/block behavior still lives in Minecraft classes, events, mixins or version-specific hooks. Nows follows that smaller approach here so the adapter is useful for porting without becoming a second Minecraft API surface.

Datapack and resource-pack sources are exposed through the same version adapter:

```java
NowsDataPacks packs = NowsMinecraft.dataPacks(context);
Path modPackDir = packs.modPackDirectory("my_mod");
packs.registerSource(PackType.SERVER_DATA, repositoryConsumer -> {
    // Create or forward Minecraft RepositorySource packs for this version.
});
```

Commands and generated JSON data are collected through the adapter as well. The command registrations are stored until a version-specific Minecraft hook applies them to a live dispatcher. JSON generation uses the loader-provided Moshi runtime, so mods can write objects, maps or explicit Moshi adapters instead of hand-built strings:

```java
NowsMinecraft.commands(context).register(dispatcher ->
        dispatcher.register(Commands.literal("my_mod").executes(command -> 1)));

NowsDataGen dataGen = NowsMinecraft.dataGen(context);
dataGen.writeJson(dataGen.recipePath("my_mod:widget"), Map.class, Map.of(
        "type", "minecraft:crafting_shapeless",
        "ingredients", List.of(Map.of("item", "minecraft:stone")),
        "result", Map.of("id", "my_mod:widget", "count", 1)
));
```

Client UI helpers are also version-backed. They cover the common cases: opening a screen, making a simple screen, adding title-screen buttons and drawing small overlays. The raw Minecraft `Screen`, `Button` and graphics object are still exposed when a mod needs deeper behavior:

```java
NowsUi ui = NowsMinecraft.ui(context);
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

Client player helpers expose both a nullable current player and a fail-fast player. They are useful for client UI, debug tools and single-player helpers; on multiplayer servers, server-authoritative values may be corrected by the server:

```java
NowsPlayerApi player = NowsMinecraft.player(context);
player.current().ifPresent(local -> {
    NowsPlayerSnapshot snapshot = player.snapshot();
    player.sendOverlayMessage(Component.literal("Hello " + snapshot.name()));
    player.setHealth(Math.min(snapshot.maxHealth(), snapshot.health() + 2.0F));
    player.setFood(20);
    player.setSelectedHotbarSlot(0);
});
```

Basic per-mod config files live in core because they are not Minecraft-version-specific:

```java
Properties config = context.configs().loadProperties("my_mod", "client");
context.configs().saveProperties("my_mod", "client", config, "My Mod client config");
```

Runtime installs these APIs through `NowsServices` when a matching `nows-mc-<version>` adapter is present. `core` stays free of `net.minecraft.*` types.

## Mixin without Java agent

`integrations/mixin` provides Nows' own `IMixinService` and `IGlobalPropertyService`. The Mixin transformer is inserted directly into `NowsClassLoader` before class definition, including synthetic class generation. Fabric Loader is not required.

## NowsInstaller

`repos/NowsInstaller` is internet-first. It downloads a release manifest from:

```text
https://files.nows.space/releases/nows/<nows-version>/<minecraft-version>/install.properties
```

and installs all listed Nows modules/libraries into the normal `.minecraft/libraries` tree before generating an inherited Official Launcher version and merging a Nows installation entry into `launcher_profiles.json`. Existing launcher profiles are preserved and `launcher_profiles.json` is backed up before modification.

The default Minecraft directory is OS-specific: `%APPDATA%\.minecraft` on Windows, `~/.minecraft` on Linux and `~/Library/Application Support/minecraft` on macOS. The installer logs the resolved path before writing files.

### GitHub Package rule

`dev.kdl:kdl4j:1.0.1` requires GitHub Packages access at **build/release time**. The installer build resolves the original JAR once and stores it inside the installer JARs under:

```text
META-INF/nows/embedded-libs/kdl4j-1.0.1.jar
```

At install time it copies that untouched JAR to:

```text
.minecraft/libraries/dev/kdl/kdl4j/1.0.1/kdl4j-1.0.1.jar
```

so players do not need `GITHUB_TOKEN`. Other libraries are downloaded over the internet according to the release manifest.

For local testing without downloads, pass `--offline --manifest <local install.properties> --artifactDir <local library root>`. Offline mode copies non-embedded artifacts from the local library root using the manifest's normal `artifact.*.path` layout.

Build credentials remain the usual:

```properties
gpr.user=YOUR_GITHUB_USER
gpr.token=TOKEN_WITH_READ_PACKAGES
```

`gpr.key` is still accepted as a compatibility alias.

## NowsGradlePlugin

Plugin id:

```kotlin
plugins {
    id("space.nows.mcnows") version "0.4.0"
}

nows {
    minecraftVersion.set("26.2")
    officialMappings.set(true)
    nowsVersion.set("0.4.0")
    addGeb.set(true)
    addNetwork.set(true)
    addMixin.set(true)
}
```

The plugin owns Minecraft development setup rather than the loader core. `nowsPrepareMinecraft` downloads the official client artifact and official `client_mappings` metadata. For modern unobfuscated Minecraft it detects Mojang-named classes and skips remapping. For older obfuscated versions it reads Mojang's ProGuard mapping file with Nows' own parser and remaps **obfuscated -> official Mojang names** with the Nows ASM remapper.

It also wires the prepared development client JAR into `compileOnly`, makes Java compilation depend on preparation, adds the matching `nows-core` API, and can add GEB, Network and Mixin compile/annotation-processor tooling to mod projects.

## Build

Fresh clone setup command:

```bash
./gradlew prepareWorkspace
```

`prepareWorkspace` syncs git submodules, checks GitHub Package credentials, GPG signing, npm and submodule paths, then builds the Java artifacts, NowsWeb, installers, offline payload, release layout and signed developer Maven layout.

```bash
./gradlew dist
```

Optional single-JAR experiment:

```bash
./gradlew allJar
```

Build the CLI and Swing UI installers (requires GitHub Packages credentials for the embedded KDL4J payload):

```bash
./gradlew :repos:NowsInstaller:assemble
```

Build the local upload layout for `files.nows.space`:

```bash
./gradlew publishLayout
```

Update the coordinated project version:

```bash
./gradlew setNowsVersion -Pnew_nows_version=0.5.0
```

The generated release files are written under `.publishing/releases/nows/<nows-version>/<minecraft-version>/`.
Developer-facing Maven artifacts are written under `.publishing/maven/`.

Build only the local Maven repository for mod developers:

```bash
./gradlew publishMavenLayout
```

Maven publishing always uses local GPG signing, even for `.publishing/maven/`.
Make sure `gpg` has a usable default signing key before running this task.

After uploading `.publishing/maven/` to `https://files.nows.space/maven/`, external mod projects can use:

```kotlin
pluginManagement {
    repositories {
        maven("https://files.nows.space/maven")
        gradlePluginPortal()
        mavenCentral()
    }
}

repositories {
    maven("https://files.nows.space/maven")
    mavenCentral()
}

dependencies {
    compileOnly("space.nows.mcnows:nows-core:0.4.0")
    compileOnly("space.nows.mcnows:nows-mc-26.2:0.4.0")
}
```

Build the same upload layout inside Docker:

```bash
docker compose run --rm nows-build
```

The CLI/UI installers are generic Java 8 installer entrypoints that choose the Minecraft target from `--minecraft` or `minecraft_version`. The published offline installer is version-specific because it embeds `install.properties`, Nows modules and `nows-mc-<minecraft-version>` for one Minecraft version:

```text
.publishing/releases/nows/<nows-version>/<minecraft-version>/installers/NowsInstaller-offline-<nows-version>-mc-<minecraft-version>.jar
```

Outputs:

```text
repos/NowsInstaller/build/libs/NowsInstaller-cli-<version>.jar
repos/NowsInstaller/build/libs/NowsInstaller-ui-<version>.jar
repos/NowsInstaller/build/libs/NowsInstaller-offline-<version>.jar
```

Build/test the Gradle plugin:

```bash
./gradlew :repos:NowsGradlePlugin:build
```

## Why this split

The intended change boundary is:

```text
stable                         expected to evolve
─────────────────────          ─────────────────────────────
nows-core contracts      <---  KDL schema/parser
classloader kernel       <---  Mixin host details
service registry         <---  event implementation
format-neutral mods      <---  logging implementation
                               Minecraft version behavior
                               installer protocol/UI
                               Gradle mappings/dev tooling
```

That lets Nows replace KDL, upgrade Mixin, change logging, support a new Minecraft layout, or rewrite the installer/Gradle tooling without turning the loader API and classloading kernel into a moving target.

## Gradle bootstrap

The source archive keeps a text-only Gradle bootstrap fallback: if `gradle-wrapper.jar` is absent, `gradlew`/`gradlew.bat` download Gradle 9.1.0 and verify its published SHA-256 before execution. `gradle-wrapper.properties` carries the same distribution checksum.
