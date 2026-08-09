# Nows architecture

Nows is intended to be a small, modern, modular Minecraft mod loader. Prefer simple architecture, explicit ownership and stable contracts over large abstraction layers.

The project uses a stable kernel / replaceable policy split: `core/` owns loader invariants, while Minecraft behavior, integrations, runtime composition and tooling live outside the kernel.

In short:

- `core/` defines what a Nows mod loader is.
- `minecraft/` knows how Nows is launched inside a Minecraft client.
- `mc/<minecraft version>/` is allowed to talk directly to that exact Minecraft version.
- `integrations/*` attach optional third-party systems to the loader.
- `runtime/` wires the selected pieces together for a launcher profile.
- `repos/*` contains distribution, developer tooling, web and optional companion projects that can evolve independently.

## Implementation snapshot

The current Nows loader version and default Minecraft target live in `gradle.properties` as `nows_version` and `minecraft_version`. Supported Minecraft adapters are the directories under `mc/`. Runtime code is built for the target Minecraft Java level, while `repos/NowsInstaller` produces Java 8-compatible installer entrypoints.

The loader's expected smoke-test path is: runtime startup, policy loading, profile-local version JAR lookup, mod dependency resolution, Mixin bootstrap, built-in title-screen mixin registration, GEB installation, network service installation, metadata-declared network channel registration, metadata-declared listener registration, loader lifecycle event dispatch, mod entrypoint execution and Minecraft main invocation. When the game is launched manually from a terminal with dummy auth values such as `--accessToken 0`, Mojang account and Realms `401`/JWT errors are expected and do not indicate a loader failure.

The normal installed profile id is:

```text
nows-<nows-version>-<minecraft-version>
```

The installer does not set `gameDir` in the launcher profile by default. The profile therefore runs on the launcher's normal game folder, and the main mod folder is:

```text
.minecraft/mods
```

Nows also scans this optional overlay:

```text
.minecraft/nows/profiles/nows-<nows-version>-<minecraft-version>/mods
```

The installer should create and log both directories. Runtime logs should show `Game mods directory` and `Optional Nows profile mods directory` during mod discovery.

## Ownership boundaries

| Module | Owns | Expected change rate |
| --- | --- | --- |
| `core/` | stable loader kernel, lifecycle contracts, services, class loading, transformation contracts and discovery abstractions | low |
| `minecraft/` | Minecraft-specific runtime behavior, launch/bootstrap details and version-sensitive compatibility | medium/high |
| `mc/<minecraft version>/` | version-specific API adapters that directly reference Minecraft classes, plus small launch-policy metadata | high |
| `integrations/kdl/` | `nows.mod.kdl` parsing through KDL4J | medium |
| `integrations/geb/` | GEB event integration | medium |
| `integrations/logging/` | Reactor logging bridge and async logging support | medium |
| `integrations/network/` | loader-level network channels, packet handler registry and transport abstraction | medium/high |
| `integrations/mixin/` | Mixin host/service details | high |
| `runtime/` | composition root that selects and connects core, Minecraft support and integrations | medium |
| `repos/NowsInstaller/` | user-facing launcher installation/update tooling and dependency delivery | high |
| `repos/NowsGradlePlugin/` | developer tooling, Minecraft dependency preparation, Mojang mappings, remapping when needed and Gradle integration | high |
| `repos/NowsApiMod/` | optional companion mod and fixture for Gradle plugin, KDL metadata, Mixin and version-specific API adapters; separate repository/submodule | high |
| `repos/NowsWeb/` | web surface for docs, downloads and release metadata; kept as a git submodule with separate frontend tooling | high |
| `example-mod/` | local example and smoke-test mod | medium |

`repos/*` projects are tooling, distribution or companion-mod surfaces. They are intentionally outside the runtime loader architecture and must not push policy back into `nows-core`.

## Stable kernel: `core/`

A change belongs in `core/` only when it is fundamental to being a mod loader rather than a Minecraft-version, metadata-format or third-party-library decision.

Core currently owns:

- `ModInitializer` - lifecycle entry contract.
- `ClassTransformer` - raw pre-definition bytecode transformation contract.
- `NowsContext` - stable runtime context, runtime-side fact, loaded-mod lookup helpers and typed service lookup.
- `NowsServices` - implementation-neutral service registry.
- `ModDescriptor`, `ModDependency`, `ModDependencyResolver`, `ModContainer`, `ModMetadataReader`, `ModDiscovery` - format-neutral mod model, dependency graph and discovery SPI.
- `NowsClassLoader` - child-first game/mod classloader, transformer chain and synthetic-class hook.

Core deliberately does not know about:

- Minecraft launch details or a particular Minecraft version layout;
- Mojang metadata or mappings;
- KDL or any metadata syntax;
- GEB or any event implementation;
- Minecraft networking or packet implementation details;
- Log4j, SLF4J or Reactor;
- Mixin;
- Gradle;
- installer, web or release tooling;
- the Official Launcher install format.

If one of those policies is replaced, `nows-core` should normally remain binary/source compatible.

## Dependency direction

```text
core
 ^
 |-- minecraft
 |-- mc/<minecraft version>
 |-- integrations/kdl
 |-- integrations/geb
 |-- integrations/logging
 |-- integrations/network
 |-- integrations/mixin --> integrations/logging
 |
 +-- runtime (composition root, depends on core, minecraft and integrations)

repos/NowsApiMod and example-mod are consumers/fixtures of the public loader surface:
core + mc/<minecraft version> + Gradle plugin + KDL + GEB + Network + Mixin
```

`core` must never depend on Minecraft, Mixin, KDL, GEB, Reactor, Gradle, installer code, web code or other integrations. Integrations may depend on `core`; `runtime/` wires the pieces together and should avoid reusable subsystem logic.

`mc/<minecraft version>` modules are not part of the kernel. They are version adapters. They may import `net.minecraft.*` directly and may break when the target Minecraft version changes; that is expected and contained.

## Distribution and installer policy

The default install is modular. Minecraft Launcher libraries contain separate Nows module JARs plus only the third-party runtime libraries that Minecraft does not already provide.

`repos/NowsInstaller/` owns the Official Launcher install protocol. The normal CLI/UI installers read an install manifest, install each declared artifact under `.minecraft/libraries`, then write an inherited Launcher version profile whose `mainClass` is `space.nows.mcnows.runtime.NowsLauncher`.

Normal installer artifact resolution is:

1. Use embedded artifacts when `artifact.<n>.source=embedded`.
2. For `source=internet`, try `artifact.<n>.url` first, normally on `files.nows.space`.
3. If that URL is missing or fails, fall back to `artifact.<n>.mavenUrl` or `mavenBaseUrl + artifact.<n>.path`.
4. For local/offline development, copy from local paths when `--offline`, `--artifactDir` or `source=local` is used.

`NowsInstaller-offline` is a separate Java 8-compatible installer JAR that embeds the install manifest and all manifest artifacts needed by Nows, then copies those embedded payloads into the Minecraft libraries directory without network access. Its Nows module payloads come from the workspace's Gradle project JAR tasks when built locally, so the same offline installer covers local-development installation for the selected `minecraft_version`.

The normal CLI/UI installers are generic installer entrypoints. They choose the target release manifest from `--minecraft`, `--nows` or their Gradle defaults and download from:

```text
https://files.nows.space/releases/nows/<nows-version>/<minecraft-version>/install.properties
```

The published offline installer is intentionally per Minecraft version because it embeds the version-specific `nows-mc-<minecraft-version>` adapter and a processed manifest for exactly one game version. Publish it with a versioned filename such as `NowsInstaller-offline-<nows-version>-mc-<minecraft-version>.jar`.

GitHub Packages-only dependencies, currently KDL4J, may be embedded into installer artifacts and copied into the Minecraft libraries directory when necessary. Normal Nows users must not need GitHub credentials just to install or run Nows.

The optional `:runtime:allJar` task is the assembly point for a later monolithic distribution. It merges Nows modules and Nows-owned third-party runtime libraries while intentionally omitting Minecraft-owned Log4j2, SLF4J, Gson, Guava and JSpecify. Moshi is a Nows-owned runtime library and is installed with its Okio/Kotlin runtime dependencies so loader APIs can expose one JSON stack consistently.

Launcher version profiles should inherit from the target vanilla Minecraft profile. When the vanilla client JAR already exists locally, the installer may copy it to the Nows version directory as a profile-local version JAR. That copy is an alias of Mojang's vanilla client JAR, not a bundled Nows library and not a redistributed loader dependency. If the vanilla JAR is missing, the version JSON may use the inherited `jar` field so the launcher can resolve the parent version.

Nows launcher profiles should inherit the launcher's normal game folder by default by omitting `gameDir` from `launcher_profiles.json`. This matches other loaders and makes `.minecraft/mods` the normal user mod folder. A profile-local game folder under `.minecraft/nows/profiles/<profile-id>/` is available only when the installer is run with `--profileGameDir` or the UI option is selected. Runtime always also scans `.minecraft/nows/profiles/<profile-id>/mods` as an optional Nows-only overlay for testing or per-profile extras. The game folder is scanned first; duplicate Nows mod ids across both folders are invalid.

The title-screen Nows badge and mod-count display are required loader proof-of-life, so they belong to `mc/<minecraft version>` rather than `repos/NowsApiMod/`. Each supported `mc/<version>` adapter can declare built-in Mixin configs through `runtime.builtinMixinConfigs` in `nows-minecraft.properties`; runtime registers those configs before mod-declared configs and passes the Nows version, Minecraft version and discovered mod count into the version adapter's client hook.

`repos/NowsApiMod/` builds a normal optional companion mod JAR. It is published as an optional artifact, but the default installer manifest does not install it by default and the loader runtime must not require it.

Release files are staged locally under `.publishing/` by `publishLayout`. That directory is intentionally ignored by Git because it is an upload staging area for `files.nows.space` backed by CDN/object storage.

`prepareWorkspace` is the canonical fresh-clone preparation task. It syncs required git submodules, validates local tools, builds Java outputs, the web surface, installers, offline payloads, release layout and signed Maven layout. It is intended for local developer testing before running narrower tasks.

`.publishing/releases/nows/<nows-version>/<minecraft-version>/` is the player/install surface. It contains the installer manifest, checksums, installers and release artifacts used by NowsInstaller.

`.publishing/maven/` is the developer surface. It is a complete local Maven repository intended to be uploaded to `https://files.nows.space/maven/` so mod developers can depend on Nows APIs and tooling. It publishes public Nows modules, version-specific `nows-mc-<minecraft-version>` adapters, integrations, runtime artifacts and the Gradle plugin marker under the `space.nows.mcnows` group.

All Maven publications must be signed with local GPG every time, including local `.publishing/maven/` builds. Unsigned Maven artifacts are considered invalid release output. The build should fail if `gpg` or a usable signing key is unavailable.

Maven POM metadata should be kept truthful and release-ready: Apache License 2.0, project SCM pointing at `NowsMC/Nows`, `TamKungZ_` / `tamkungz` / `dev@tamkungz.me` as maintainer, and `HollZaterQ` / `hollzaterq` as tester.

The expected upload backing is `files.nows.space` in front of object storage/CDN infrastructure such as Backblaze B2 plus Gcore. Release generation does not upload anything; it only produces the local tree, manifest and checksum file for manual publishing.

Docker is allowed as a reproducible build shell for this multi-project workspace. The Docker path should build the same `.publishing/` layout as local Gradle and should not introduce a separate release protocol.

## Common build commands

Use this from a fresh clone intended for local testing:

```bash
./gradlew prepareWorkspace
```

Use this to stage release files for the default Minecraft target from `gradle.properties`:

```bash
./gradlew publishLayout
```

Use this to stage a different supported Minecraft target:

```bash
./gradlew -Pminecraft_version=<minecraft-version> publishLayout
```

Use this to bump the coordinated Nows version:

```bash
./gradlew setNowsVersion -Pnew_nows_version=<version>
```

Use this to inspect the coordinated version state:

```bash
./gradlew versionReport
```

After `publishLayout`, the offline installer for the selected release is expected at:

```text
.publishing/releases/nows/<nows-version>/<minecraft-version>/installers/NowsInstaller-offline-<nows-version>-mc-<minecraft-version>.jar
```

## Gradle and mappings policy

`repos/NowsGradlePlugin/` owns developer setup:

- Minecraft dependency preparation;
- official Mojang mappings;
- remapping when necessary;
- development classpaths;
- Gradle integration and related workarounds.

Official Mojang mappings are the canonical development namespace. For modern Minecraft versions where Mojang distributes usable named jars/mappings, do not perform unnecessary remapping. For older versions, Gradle tooling may prepare or remap the development jar to official Mojang mappings.

Mapping/remapping logic should not leak into the runtime loader unless runtime remapping is actually required.

Nows owns its development remapper. The Gradle plugin reads Mojang's official ProGuard mapping file directly and remaps class, field and method references through ASM. Do not depend on Tiny Remapper, Mapping IO or Fabric tooling for this path.

The Gradle plugin must be usable both from a published plugin artifact and as an included build inside this monorepo. Local fixtures such as `repos/NowsApiMod/` should exercise the same plugin path that an external mod project would use.

The plugin may reuse `.nows/minecraft/<version>/client-dev.jar` as a local cache, but each project task should write its own build output. Two Gradle tasks must not claim the same output file.

The repository policy is intentionally not Fabric-based. Use Maven Central and SpongePowered Maven for SpongePowered Mixin. Do not add `https://maven.fabricmc.net/` just to obtain Mixin.

## Version management

`gradle.properties` is the source of truth for the monorepo's `nows_version`.
Build logic generates installer defaults, runtime version resources, Gradle
plugin defaults and release manifests from that value. `install.properties.template`
is only a template; `publishLayout` rewrites Nows module coordinates, paths,
URLs and checksums before staging a release.

`repos/NowsApiMod` is also usable as a standalone repository, so it keeps its own
`gradle.properties`. Use the root helper when bumping releases:

```bash
./gradlew setNowsVersion -Pnew_nows_version=<version>
```

That updates both the monorepo and standalone `NowsApiMod` property files. Use
`./gradlew versionReport` to print the current coordinated version.

## Technical rules

- Do not use Java Agent, `premain` or `java.lang.instrument`.
- Do not depend on Fabric Loader or another mod loader.
- Mixin itself is allowed, but use the SpongePowered coordinate `org.spongepowered:mixin`.
- Do not bundle libraries already provided by the target Minecraft version unless isolation makes it strictly necessary.
- Keep build-time-only tools out of runtime artifacts.
- Annotation processors must not accidentally become runtime dependencies.
- Avoid reflection when a normal typed API is reasonable.
- Avoid global static state unless required by a third-party integration.
- Prefer Java APIs/contracts in core that can survive replacing an integration later.
- Be deliberate about Gradle dependency exposure:
  - use `api` when dependency types appear in a module's public API;
  - use `implementation` when a dependency is internal;
  - use `compileOnly` when Minecraft/runtime is expected to provide the dependency.

## GEB event policy

`integrations/geb/` owns the Nows-facing event surface. Runtime installs one GEB bus, loads GEB-generated dispatchers from the game/mod classloader, registers Nows-owned lifecycle dispatchers and exposes both raw `GEB` and the small `NowsEvents` facade through `NowsServices`.

Mods may declare listener classes in metadata with `listener` or `geb-listener`. Runtime instantiates those no-argument listener classes with the game classloader and registers them before mod entrypoints run. Listener registration is metadata policy and belongs in the GEB integration/runtime composition, not in `core`.

Nows-owned loader lifecycle events live in `integrations/geb/event/`. Mods should observe those events by implementing `NowsLifecycleListener`; Nows provides built-in dispatchers for them. This avoids relying on every mod's GEB annotation processor to generate a dispatcher with the same event-class-derived name, which can collide when multiple mods listen to the same loader event. Regular GEB `@Listen` methods remain appropriate for mod-owned custom events and dispatchers generated inside a mod.

GEB remains optional from the perspective of `core`. A mod or optional companion surface that does not use GEB should be able to compile against `nows-core` without depending on GEB types.

## Network policy

`integrations/network/` owns the mod-facing network channel registry, packet handler API and `NetworkTransport` abstraction. It deliberately does not import `net.minecraft.*` or version-specific packet classes. Payloads are backed by Netty `ByteBuf` because Minecraft already ships and uses Netty for networking; Nows should use Netty as a compile-time API only and must not bundle a second Netty copy into the loader distribution. Version adapters or later Minecraft integration code should install the concrete transport that knows how to send packets through the actual game connection.

Mods may declare channels in metadata with `network-channel`, `network`, `clientbound-channel` or `serverbound-channel`. Runtime registers those channels before entrypoints run and exposes `NowsNetworking` through `NowsServices`. Mod entrypoints may then register handlers for declared or dynamically-created channels.

Until a concrete Minecraft transport is installed, `NowsNetworking.send(...)` returns `false` rather than pretending a packet was sent. Receiving and handler dispatch are still fully testable at the integration layer. This keeps the public channel/handler contract stable while allowing each `mc/<version>` adapter to handle the details of custom payload registration and connection state later.

## Minecraft API adapter policy

`mc/<minecraft version>/` owns the basic developer-facing API layer that must directly reference Minecraft classes. Common mod tasks such as resource ids/keys/tags, registering `Item`, `Block`, `BlockItem`, food, simple weapons, armor, creative tabs, command registration collectors, generated JSON data and datapack/resource-pack repository sources belong there, not in `core`, `minecraft`, `runtime` or generic integrations.

Each supported adapter should expose the same Nows package/class names, for example `space.nows.mcnows.mc.api.NowsMinecraft`, `NowsRegistryApi` and `NowsDataPacks`, while using that Minecraft version's real classes internally. This lets 26.2 use `Identifier` and `ResourceKey` setup while 1.20.1 uses `ResourceLocation`, without forcing mods to rewrite their Nows-facing calls.

Runtime installs the version adapter by reflecting `space.nows.mcnows.mc.internal.NowsMinecraftIntegration` and registering its services into `NowsServices`. That reflection is a composition boundary only; mod code should use the typed API from the selected `nows-mc-<version>` artifact. If no adapter is present, runtime should continue to launch with a clear INFO log and without those optional services.

Datapack and command management are intentionally source registries first. Mods can declare or collect `RepositorySource` instances and command dispatcher consumers; later game hooks may feed those sources into Minecraft's live pack repositories and command dispatchers at the correct lifecycle point for each version. Generated data helpers write JSON under `.minecraft/nows/generated` and are developer convenience, not a replacement for Minecraft's full data generator.

The registry API is a portability convenience, not a full replacement for Minecraft APIs. Common convenience helpers may wrap simple behavior through small logic interfaces such as `NowsItemLogic` and `NowsBlockLogic`. Full-detail Minecraft behavior must remain possible through custom `Item`/`Block` factories so mod authors can override version-specific methods directly when the basic helper layer is too small.

`core` may own Minecraft-neutral developer conveniences such as side checks and per-mod config files. Runtime should register those core services before Minecraft-version adapter services.

## Logging policy

`integrations/logging/` owns Nows logging policy. Runtime and integrations should get loggers through `NowsLog` instead of configuring third-party logging directly.

Loader startup should be observable from INFO logs. Nows should log phase start/end, selected runtime/policy facts, mod discovery, classloader setup, built-in Mixin config registration, transformer loading, service installation, metadata-declared network channel registration, metadata-declared listener registration, lifecycle dispatch points and entrypoint execution without printing access tokens or other launcher secrets.

The default backend is `slf4j`, which flows into Minecraft's existing SLF4J -> Log4j2 backend. For local diagnostics, set:

```text
-Dnows.logging.backend=slf4j
-Dnows.logging.backend=console
-Dnows.logging.backend=verbose-console
-Dnows.logging.backend=jdk
```

If the requested backend is unavailable, Nows falls back to JDK logging.

## Dependency preferences

Important libraries currently intended for Nows include the following. Their exact versions belong in `gradle.properties`, not in this architecture document:

- `foo.zaaarf.geb:processor`
- `foo.zaaarf.geb:core`
- `dev.kdl:kdl4j`
- Reactor logging through `io.projectreactor:reactor-core`
- Log4j2 Async Logger support through Disruptor
- SpongePowered Mixin through `org.spongepowered:mixin`

KDL4J may come from GitHub Packages. Release/build tooling may need credentials to resolve it, but installer/runtime users should not.

## Why the metadata model is generic

Core stores common mod facts such as id, name, version, target Minecraft version, target side, description, authors, contributors, licenses, icon, contact links, arbitrary properties and dependency declarations because those concepts are useful across metadata formats. It also stores feature declarations as `Map<String, List<String>>` instead of fields like `mixins`, `entrypoints` or `listeners`. The KDL integration can therefore introduce a new declaration without forcing a core record/API revision.

For example:

```kdl
mod id="example" name="Example Mod" version="1.0.0" minecraft="26.2" side="client" {
    info {
        description "Small example mod."
        author "ExampleDev"
        license "Apache-2.0"
    }

    links {
        homepage "https://example.com"
        sources "https://github.com/example/mod"
    }

    compatibility {
        depends "other_mod" version=">=1.0.0"
        incompatible-with "bad_mod" reason="Known broken integration"
    }

    load-order {
        after "other_mod"
    }

    runtime {
        network-channel "example:main"
        listener "example.ExampleLifecycleListener"
        entrypoint "example.Mod"
        mixin "example.mixins.json"
    }

    features {
        future-feature "some.value"
    }
}
```

Only the integration that understands `future-feature` needs to change. KDL groups such as `info`, `links`, `compatibility`, `load-order`, `properties`, `runtime` and `features` are readability wrappers; equivalent flat nodes remain valid and map to the same descriptor. Runtime and integrations may still agree on well-known declaration keys such as `entrypoint`, `transformer`, `mixin`, `network-channel` and `listener`; those are loader/integration policy, not hard-coded fields in `core`.

`side` is typed as `NowsSide` rather than treated as an arbitrary property. Metadata formats should map `client`, `server` and `both`/`common` onto that enum. The current runtime is a client launcher, so it validates discovered mods against `NowsSide.CLIENT` and rejects server-only mods before loading mod classes. A future dedicated server runtime should set `NowsSide.SERVER` at the composition root and reuse the same compatibility contract.

`NowsContext` should provide ergonomic loaded-mod lookup and runtime-side access over this model, such as checking whether a mod id is present, retrieving a `ModDescriptor` by id or checking `context.side()`. That lookup belongs in core because it is about the stable loaded-mod graph/runtime fact, not about KDL, Minecraft, GEB or Mixin.

Dependency resolution belongs to the core mod graph because it is metadata-format neutral. Runtime supplies provided ids such as `minecraft`, `nows` and `nows-loader`, then asks core to validate required dependencies, reject conflicts and sort mods by required dependencies plus `load-before`/`load-after` rules before creating the game/mod classloader. Version-specific Minecraft compatibility checks may still run after this to validate side and Minecraft-version policy.

## Compatibility philosophy

Do not optimize the core around one Minecraft version.

Put version-sensitive behavior behind Minecraft/tooling modules so changes in Minecraft, Gradle, mappings, Mixin or metadata formats do not force changes to `core`.

Per-version Minecraft API code belongs under `mc/<minecraft version>/`. These modules are allowed to import `net.minecraft.*` directly for things like blocks, client UI, screens and mod-menu hooks. The generic `minecraft` module should keep stable launch/discovery behavior; `core` must stay independent from every Minecraft version.

Per-version facts that are data rather than reusable Java behavior also live under `mc/<minecraft version>/nows-minecraft.properties`. The `minecraft` module packages those files and exposes them through `MinecraftVersionPolicy`. Runtime uses this policy for facts such as the client main class while keeping Minecraft classes in the game classloader.

Per-version client UI hooks also belong under `mc/<minecraft version>/`. For example, Minecraft 1.20.1 renders title screens through `GuiGraphics`, while Minecraft 26.2 extracts render state through `GuiGraphicsExtractor`; those differences should be handled by the matching version adapter, not by core or an optional companion mod.

Nows follows the inherited Launcher-profile style: inherit the vanilla version profile, add Nows libraries and use a Nows bootstrap main class. Some other loaders may copy the vanilla client JAR into their own version directory; if Nows does that, it is only a launcher-version alias of the vanilla JAR. Nows should not make `net.minecraft.` parent-first; doing so risks resolving game classes from the wrong loader/classpath and can interfere with other launcher profiles.

## Companion API mod

`repos/NowsApiMod/` exists for useful features that should not be required by the loader itself. Examples include higher-level helper APIs, UI conveniences, mod-menu-style integration, test blocks/items and other user-facing helpers.

Rules for `NowsApiMod`:

- It may depend on public Nows APIs and `mc/<minecraft version>` adapters.
- It must be optional; the loader must run without it.
- It may keep `src/mc_<minecraft_version>/` source sets for optional features that need direct Minecraft APIs, but required loader-visible behavior belongs in `mc/<minecraft version>/`.
- It is a good place to test new KDL declaration names before promoting them to a stable integration.
- It should exercise the Nows Gradle plugin rather than bypassing it with special monorepo-only wiring.
- It should stay in its own repository so release cadence and API churn do not force loader releases.

## Code quality

Prefer:

- small classes;
- explicit data flow;
- immutable records/value objects where appropriate;
- composition over inheritance;
- meaningful errors;
- deterministic behavior;
- minimal hidden magic.

Do not create interfaces with only one implementation unless the boundary provides a real architectural benefit. Do not create factories/builders/managers/providers merely for naming symmetry.

## How to make changes

Before editing:

1. Inspect the existing implementation.
2. Understand which module owns the problem.
3. Prefer fixing dependency/module ownership instead of adding duplicate dependencies to every consumer.
4. Avoid unrelated refactors.
5. Do not rewrite working architecture merely to make it stylistically cleaner.

When a compile error occurs, fix its root cause rather than suppressing it. For example, if module A publicly exposes a type from dependency X and module B consumes A, prefer making X an `api` dependency of A rather than manually adding X everywhere downstream.

Work incrementally. When fixing an issue, modify only files required for that issue unless a nearby architectural problem directly causes it.

Do not silently redesign Nows.
