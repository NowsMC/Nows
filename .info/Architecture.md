You are working directly on the Nows Minecraft mod loader repository.

Nows is intended to be a small, modern, modular mod loader. Prefer simple architecture and explicit ownership over large abstraction layers.

## Architecture

Treat these boundaries as intentional:

- `core/`
  - Stable loader kernel.
  - Should change rarely.
  - Must not depend on Minecraft, Mixin, KDL, GEB, Reactor, Gradle, installer code, or other integrations.
  - Contains only concepts fundamental to being a mod loader: mod lifecycle/contracts, services, class loading, transformation contracts, discovery abstractions, etc.

- `minecraft/`
  - Minecraft-specific runtime behavior.
  - Minecraft launch/bootstrap details belong here rather than in core.

- `integrations/`
  - Replaceable integrations.
  - Current examples: Mixin, KDL4J, GEB, logging.
  - Integrations may depend on core, but core must never depend on integrations.

- `runtime/`
  - Composition root.
  - Connects core, Minecraft support, and integrations together.
  - Avoid putting reusable subsystem logic here.

- `repos/NowsInstaller/`
  - User-facing installation/update tooling.
  - Internet-first installation is acceptable for now.
  - GitHub Packages-only dependencies may be embedded into the installer and copied into the Minecraft libraries directory when necessary.

- `repos/NowsGradlePlugin/`
  - Developer tooling.
  - Owns Minecraft dependency preparation, official Mojang mappings, remapping when necessary, development classpaths, Gradle integration, and related workarounds.
  - Mapping/remapping logic should not leak into the runtime loader unless runtime remapping is actually required.

## Important technical rules

- Do not use Java Agent, `premain`, or `java.lang.instrument`.
- Official Mojang mappings are the canonical development namespace.
- Do not bundle libraries already provided by the target Minecraft version unless isolation makes it strictly necessary.
- Be deliberate about Gradle dependency exposure:
  - use `api` when a dependency's types appear in a module's public API;
  - use `implementation` when it is internal;
  - use `compileOnly` when Minecraft/runtime is expected to provide it.
- Keep build-time-only tools out of runtime artifacts.
- Annotation processors must not accidentally become runtime dependencies.
- Avoid depending on Fabric Loader or another mod loader.
- Mixin itself is allowed.
- Avoid reflection when a normal typed API is reasonable.
- Avoid global static state unless required by a third-party integration.
- Prefer Java APIs/contracts in core that can survive replacing an integration later.

## Dependency preferences

Important libraries currently intended for Nows include:

- `foo.zaaarf.geb:processor:0.4.9`
- `foo.zaaarf.geb:core:0.5.4`
- `dev.kdl:kdl4j:1.0.1`
- Reactor logging
- Log4j2 Async Logger
- SpongePowered/Fabric Mixin

KDL4J may come from GitHub Packages. Do not require normal Nows users to configure GitHub credentials just to install/run Nows.

## How to make changes

Before editing:

1. Inspect the existing implementation.
2. Understand which module owns the problem.
3. Prefer fixing dependency/module ownership instead of adding duplicate dependencies to every consumer.
4. Avoid unrelated refactors.
5. Do not rewrite working architecture merely to make it stylistically cleaner.

When a compile error occurs, fix its root cause rather than suppressing it.

For example, if module A publicly exposes a type from dependency X and module B consumes A, prefer making X an `api` dependency of A rather than manually adding X everywhere downstream.

## Compatibility philosophy

Do not optimize the core around one Minecraft version.

Put version-sensitive behavior behind Minecraft/tooling modules so changes in Minecraft, Gradle, mappings, Mixin, or metadata formats do not force changes to `core`.

For modern Minecraft versions where Mojang distributes usable named jars/mappings, do not perform unnecessary remapping.

For older versions, Gradle tooling may prepare/remap the development jar to official Mojang mappings.

## Code quality

Prefer:

- small classes;
- explicit data flow;
- immutable records/value objects where appropriate;
- composition over inheritance;
- meaningful errors;
- deterministic behavior;
- minimal hidden magic.

Do not create interfaces with only one implementation unless the boundary provides a real architectural benefit.

Do not create factories/builders/managers/providers merely for naming symmetry.

## Working style

Work incrementally.

When fixing an issue, modify only files required for that issue unless a nearby architectural problem directly causes it.

At the end of each task, report:

- what was changed;
- why;
- files modified;
- any architecture decision made;
- unresolved issues or likely next steps.

Do not silently redesign Nows.