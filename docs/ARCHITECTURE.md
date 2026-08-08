# Nows architecture

Nows is intended to be a small, modern, modular Minecraft mod loader. Prefer simple architecture, explicit ownership and stable contracts over large abstraction layers.

The project uses a stable kernel / replaceable policy split: `core/` owns loader invariants, while Minecraft behavior, integrations, runtime composition and tooling live outside the kernel.

## Ownership boundaries

| Module | Owns | Expected change rate |
| --- | --- | --- |
| `core/` | stable loader kernel, lifecycle contracts, services, class loading, transformation contracts and discovery abstractions | low |
| `minecraft/` | Minecraft-specific runtime behavior, launch/bootstrap details and version-sensitive compatibility | medium/high |
| `integrations/kdl/` | `nows.mod.kdl` parsing through KDL4J | medium |
| `integrations/geb/` | GEB event integration | medium |
| `integrations/logging/` | Reactor logging bridge and async logging support | medium |
| `integrations/mixin/` | Mixin host/service details | high |
| `runtime/` | composition root that selects and connects core, Minecraft support and integrations | medium |
| `repos/NowsInstaller/` | user-facing launcher installation/update tooling and dependency delivery | high |
| `repos/NowsGradlePlugin/` | developer tooling, Minecraft dependency preparation, Mojang mappings, remapping when needed and Gradle integration | high |
| `repos/NowsWeb/` | web surface for docs, downloads and release metadata; kept as a git submodule with separate frontend tooling | high |
| `example-mod/` | local example and smoke-test mod | medium |

`repos/*` projects are tooling or distribution surfaces. They are intentionally outside the runtime loader architecture and must not push policy back into `nows-core`.

## Stable kernel: `core/`

A change belongs in `core/` only when it is fundamental to being a mod loader rather than a Minecraft-version, metadata-format or third-party-library decision.

Core currently owns:

- `ModInitializer` - lifecycle entry contract.
- `ClassTransformer` - raw pre-definition bytecode transformation contract.
- `NowsContext` - stable runtime context plus typed service lookup.
- `NowsServices` - implementation-neutral service registry.
- `ModDescriptor`, `ModContainer`, `ModMetadataReader`, `ModDiscovery` - format-neutral mod model and discovery SPI.
- `NowsClassLoader` - child-first game/mod classloader, transformer chain and synthetic-class hook.

Core deliberately does not know about:

- Minecraft launch details or a particular Minecraft version layout;
- Mojang metadata or mappings;
- KDL or any metadata syntax;
- GEB or any event implementation;
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
 |-- integrations/kdl
 |-- integrations/geb
 |-- integrations/logging
 |-- integrations/mixin --> integrations/logging
 |
 +-- runtime (composition root, depends on core, minecraft and integrations)
```

`core` must never depend on Minecraft, Mixin, KDL, GEB, Reactor, Gradle, installer code, web code or other integrations. Integrations may depend on `core`; `runtime/` wires the pieces together and should avoid reusable subsystem logic.

## Distribution and installer policy

The default install is modular. Minecraft Launcher libraries contain separate Nows module JARs plus only the third-party runtime libraries that Minecraft does not already provide.

`repos/NowsInstaller/` owns the Official Launcher install protocol. The normal CLI/UI installers read an install manifest, install each declared artifact under `.minecraft/libraries`, then write an inherited Launcher version profile whose `mainClass` is `space.nows.mcnows.runtime.NowsLauncher`.

Normal installer artifact resolution is:

1. Use embedded artifacts when `artifact.<n>.source=embedded`.
2. For `source=internet`, try `artifact.<n>.url` first, normally on `nows.space`.
3. If that URL is missing or fails, fall back to `artifact.<n>.mavenUrl` or `mavenBaseUrl + artifact.<n>.path`.
4. For local/offline development, copy from local paths when `--offline`, `--artifactDir` or `source=local` is used.

`NowsInstaller-offline` is a separate Java 8-compatible installer JAR that embeds the install manifest and all manifest artifacts needed by Nows, then copies those embedded payloads into the Minecraft libraries directory without network access.

GitHub Packages-only dependencies, currently KDL4J, may be embedded into installer artifacts and copied into the Minecraft libraries directory when necessary. Normal Nows users must not need GitHub credentials just to install or run Nows.

The optional `:runtime:allJar` task is the assembly point for a later monolithic distribution. It merges Nows modules and Nows-owned third-party runtime libraries while intentionally omitting Minecraft-owned Log4j2, SLF4J, Gson, Guava and JSpecify.

## Gradle and mappings policy

`repos/NowsGradlePlugin/` owns developer setup:

- Minecraft dependency preparation;
- official Mojang mappings;
- remapping when necessary;
- development classpaths;
- Gradle integration and related workarounds.

Official Mojang mappings are the canonical development namespace. For modern Minecraft versions where Mojang distributes usable named jars/mappings, do not perform unnecessary remapping. For older versions, Gradle tooling may prepare or remap the development jar to official Mojang mappings.

Mapping/remapping logic should not leak into the runtime loader unless runtime remapping is actually required.

## Technical rules

- Do not use Java Agent, `premain` or `java.lang.instrument`.
- Do not depend on Fabric Loader or another mod loader.
- Mixin itself is allowed.
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

## Logging policy

`integrations/logging/` owns Nows logging policy. Runtime and integrations should get loggers through `NowsLog` instead of configuring third-party logging directly.

The default backend is `slf4j`, which flows into Minecraft's existing SLF4J -> Log4j2 backend. For local diagnostics, set:

```text
-Dnows.logging.backend=slf4j
-Dnows.logging.backend=console
-Dnows.logging.backend=verbose-console
-Dnows.logging.backend=jdk
```

If the requested backend is unavailable, Nows falls back to JDK logging.

## Dependency preferences

Important libraries currently intended for Nows include:

- `foo.zaaarf.geb:processor:0.4.9`
- `foo.zaaarf.geb:core:0.5.4`
- `dev.kdl:kdl4j:1.0.1`
- Reactor logging through `io.projectreactor:reactor-core`
- Log4j2 Async Logger support through Disruptor
- SpongePowered/Fabric Mixin

KDL4J may come from GitHub Packages. Release/build tooling may need credentials to resolve it, but installer/runtime users should not.

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

## Compatibility philosophy

Do not optimize the core around one Minecraft version.

Put version-sensitive behavior behind Minecraft/tooling modules so changes in Minecraft, Gradle, mappings, Mixin or metadata formats do not force changes to `core`.

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
