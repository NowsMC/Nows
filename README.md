# Nows

Nows is an experimental Minecraft Java mod loader for developers who want one more option for building mods across Minecraft versions.

It is not intended to replace Minecraft, Mojang's APIs, or every existing loader ecosystem. Nows is a layer between a mod and Minecraft: it keeps a small loader kernel stable, then puts version-sensitive Minecraft behavior behind `mc/<version>` adapters so mod developers can reuse more of their own code when moving between supported game versions.

This portability layer is still incomplete. Nows does not cover 100% of Minecraft APIs, and advanced mod behavior should still use Minecraft's own classes, events, mixins, or version-specific hooks directly when needed.

- Website/domain: **https://nows.space**
- Java/Maven namespace: **`space.nows.mcnows`**
- Current source version: **0.4.0**
- Current target: **Minecraft Java 26.2**
- Development names: **official Mojang mappings**
- Runtime style: no Java agent, `premain`, or `java.lang.instrument`

## Support policy

Nows releases support for Minecraft **major versions only**. Patch/minor compatibility can improve inside an adapter when practical, but the release target is the selected major Minecraft version rather than every point release.

Current supported adapter directories live under [`mc/`](mc/):

- `mc/26.2`
- `mc/1.20.1`

## Project shape

```text
Nows/
├─ core/                         stable loader kernel, no external runtime stack
├─ minecraft/                    Minecraft launch/version policy
├─ mc/<version>/                 version-specific Minecraft API adapters
├─ integrations/
│  ├─ kdl/                       nows.mod.kdl metadata parser
│  ├─ geb/                       GEB event integration
│  ├─ logging/                   logging bridge and async logging support
│  ├─ network/                   Nows network channels and transport facade
│  └─ mixin/                     SpongePowered Mixin service integration
├─ runtime/                      composition root and launcher entrypoint
├─ repos/
│  ├─ NowsInstaller/             installer
│  ├─ NowsGradlePlugin/          mod development Gradle plugin
│  ├─ NowsApiMod/                optional companion/API mod
│  └─ NowsWeb/                   website/download surface
└─ example-mod/                  local example mod
```

The core rule is simple: `nows-core` should contain only contracts that can survive loader policy changes. Minecraft code, metadata formats, event buses, logging, Mixin, installer behavior and Gradle tooling live outside core.

Read more:

- [Architecture](docs/ARCHITECTURE.md)
- [Dependency ownership](docs/DEPENDENCY_OWNERSHIP.md)
- [Mod development guide](docs/MOD_DEVELOPMENT.md)
- [Build and release notes](docs/BUILD_AND_RELEASE.md)
- [Minecraft version adapters](mc/README.md)

## Quick build

Fresh clone setup and full local validation:

```bash
./gradlew prepareWorkspace
```

Build normal distribution artifacts:

```bash
./gradlew dist
```

Stage release files for the default Minecraft target:

```bash
./gradlew publishLayout
```

Build/test the Gradle plugin:

```bash
./gradlew :repos:NowsGradlePlugin:build
```

## Mod metadata

Nows currently recommends `nows.mod.kdl` for human-facing mod metadata. KDL is an integration, not a core dependency: `nows-core` sees only generic `ModDescriptor` data.

Minimal example:

```kdl
mod id="my_mod" name="My Mod" version="1.0.0" minecraft="26.2" side="client" {
    runtime {
        entrypoint "com.example.MyMod"
        mixin "my_mod.mixins.json"
    }
}
```

See [Mod development guide](docs/MOD_DEVELOPMENT.md) for dependency metadata, lifecycle listeners, registry helpers, networking, UI and config examples.

## Gradle plugin

External mod projects can use the Nows Gradle plugin once the developer Maven layout is published to `https://files.nows.space/maven/`:

```kotlin
plugins {
    id("space.nows.mcnows") version "0.4.0"
}

nows {
    minecraftVersion.set("26.2")
    nowsVersion.set("0.4.0")
}
```

The plugin applies Gradle's Java plugin and owns Minecraft development setup, official Mojang mappings, the matching Minecraft adapter, common Nows integrations and `nows.mod.kdl` resource expansion. Those policies do not belong in `nows-core`.

## License

Nows is licensed under the Apache License, Version 2.0. Earlier private development commits may have carried MIT license text; those commits were kept in a private development repository and were not public releases of Nows. The project license for Nows source, documentation and release artifacts is Apache License 2.0 from this point forward.
