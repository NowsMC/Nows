# Nows architecture

Nows uses a **stable kernel / replaceable policy** split.

## Stable kernel: `core/`

A change belongs in core only when it is a loader invariant rather than a Minecraft-version or third-party-library decision.

Core currently owns:

- `ModInitializer` — lifecycle entry contract.
- `ClassTransformer` — raw pre-definition bytecode transformation contract.
- `NowsContext` — stable runtime context plus typed service lookup.
- `NowsServices` — implementation-neutral service registry.
- `ModDescriptor`, `ModContainer`, `ModMetadataReader`, `ModDiscovery` — format-neutral mod model/discovery SPI.
- `NowsClassLoader` — child-first game/mod classloader, transformer chain and synthetic-class hook.

Core deliberately does **not** know about:

- KDL or any metadata syntax;
- GEB or any event implementation;
- Log4j, SLF4J or Reactor;
- Mixin;
- Mojang metadata/mappings;
- a particular Minecraft version layout;
- the Official Launcher install format;
- Gradle.

If any of those are replaced, `nows-core` should normally remain binary/source compatible.

## Replaceable policy modules

| Module | Owns | Expected change rate |
| --- | --- | --- |
| `minecraft/` | game version lookup, launch arguments, compatibility | medium/high |
| `integrations/kdl/` | `nows.mod.kdl` parsing | medium |
| `integrations/geb/` | event implementation | medium |
| `integrations/logging/` | Reactor logging bridge / async logging support | medium |
| `integrations/mixin/` | Mixin host/service details | high |
| `runtime/` | selects and composes integrations | medium |
| `repos/NowsInstaller/` | launcher/install protocol and dependency delivery | high |
| `repos/NowsGradlePlugin/` | mappings, Gradle compatibility, developer setup | high |

## Dependency direction

```text
core
 ↑
 ├── minecraft
 ├── integrations/kdl
 ├── integrations/geb
 ├── integrations/logging
 └── integrations/mixin ──> integrations/logging
          ↑
          └──────── runtime (composition root)
```

`repos/*` are tooling repositories and are not part of the runtime dependency graph.

## Distribution rule

The default install is modular. Minecraft Launcher libraries contain separate Nows module JARs plus only the third-party runtime libraries that Minecraft does not already provide.

The optional `:runtime:allJar` task is the assembly point for a later monolithic distribution. It merges Nows modules and Nows-owned third-party runtime libraries, while intentionally omitting Minecraft-owned Log4j2, SLF4J, Gson, Guava and JSpecify.

## Why the metadata model is generic

Core stores child declarations as `Map<String, List<String>>` instead of fields like `mixins` or `entrypoints`. The KDL integration can therefore introduce a new declaration without forcing a core record/API revision.

For example:

```kdl
mod id="example" version="1.0.0" minecraft="26.2" {
    entrypoint "example.Mod"
    mixin "example.mixins.json"
    future-feature "some.value"
}
```

Only the integration that understands `future-feature` needs to change.
