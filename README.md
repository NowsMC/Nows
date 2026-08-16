# Nows

Nows is a small, experimental Minecraft Java mod loader.

The loader keeps its kernel in `core/` and puts Minecraft-specific behavior behind
`mc/<version>` adapters. Code that depends on metadata formats, event buses, logging,
Mixin, installers or Gradle tooling lives outside the kernel.

Nows is still incomplete. It is useful for experimenting with a stable loader surface
across supported Minecraft versions, but mods can still use Minecraft classes, mixins
and version-specific hooks directly when that is the simpler choice.

## Project Facts

- Website/domain: <https://nows.space>
- Java/Maven namespace: `space.nows.mcnows`
- Development mappings: official Mojang mappings
- Runtime style: no Java agent, `premain` or `java.lang.instrument`

The current Nows version and default Minecraft target are defined in
[`gradle.properties`](gradle.properties). Supported adapters are the directories under
[`mc/`](mc/).

Nows releases target Minecraft major versions. Patch/minor compatibility can improve
inside an adapter, but the project does not try to promise every point release.

## Repository Layout

```text
core/                         stable loader kernel
minecraft/                    Minecraft launch/version policy
mc/<version>/                 version-specific Minecraft adapters
integrations/kdl/             nows.mod.kdl metadata parser
integrations/geb/             GEB event integration
integrations/logging/         logging bridge and async logging
integrations/network/         loader network channels
integrations/mixin/           SpongePowered Mixin integration
runtime/                      launcher entrypoint and composition root
repos/NowsInstaller/          installer
repos/NowsGradlePlugin/       mod development Gradle plugin
repos/NowsApiMod/             optional companion/API mod
repos/NowsWeb/                optional website submodule
example-mod/                  local example mod
```

The main boundary is:

- `core/` owns contracts that should survive loader policy changes.
- `mc/<version>/` may reference `net.minecraft.*` directly.
- `runtime/` wires the selected pieces together.
- `repos/*` contains tooling, distribution and companion surfaces.

For the full ownership rules, see [Architecture](docs/ARCHITECTURE.md) and
[Dependency ownership](docs/DEPENDENCY_OWNERSHIP.md).

## Build

Prepare a fresh clone and run the broad local validation path:

```bash
./gradlew prepareWorkspace
```

Build distribution artifacts:

```bash
./gradlew dist
```

Stage release files:

```bash
./gradlew publishLayout
```

Build/test the Gradle plugin:

```bash
./gradlew :repos:NowsGradlePlugin:build
```

Docker can be used as a reproducible build shell:

```bash
docker compose run --rm nows-build
```

Signed release layout:

```bash
docker compose run --rm nows-release
```

Website submodule:

```bash
git submodule update --init repos/NowsWeb
docker compose up nows-web
```

## Development Docs

- [Architecture](docs/ARCHITECTURE.md)
- [Dependency ownership](docs/DEPENDENCY_OWNERSHIP.md)
- [Mod development guide](docs/MOD_DEVELOPMENT.md)
- [Build and release notes](docs/BUILD_AND_RELEASE.md)
- [Minecraft version adapters](mc/README.md)

## License

Apache License 2.0. See [LICENSE](LICENSE).
