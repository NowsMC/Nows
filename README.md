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

## KDL is replaceable metadata

The core never imports KDL4J. `integrations/kdl` implements the `ModMetadataReader` SPI and turns `nows.mod.kdl` into a generic `ModDescriptor`.

```kdl
mod id="my_mod" name="My Mod" version="1.0.0" minecraft="26.2" {
    entrypoint "com.example.MyMod"
    transformer "com.example.MyTransformer"
    mixin "my_mod.mixins.json"
}
```

Unknown future declaration names can be retained without changing core because declarations are stored by key rather than by fixed record fields.

## GEB without coupling core to GEB

`integrations/geb` registers the GEB instance in `NowsServices`. Mods that want it can use:

```java
GEB bus = GebIntegration.eventBus(context);
```

A mod that does not care about GEB still compiles against `nows-core` without pulling GEB into its API.

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
    addMixin.set(true)
}
```

The plugin owns Minecraft development setup rather than the loader core. `nowsPrepareMinecraft` downloads the official client artifact and official `client_mappings` metadata. For modern unobfuscated Minecraft it detects Mojang-named classes and skips remapping. For older obfuscated versions it reads Mojang's ProGuard mapping file with Nows' own parser and remaps **obfuscated -> official Mojang names** with the Nows ASM remapper.

It also wires the prepared development client JAR into `compileOnly`, makes Java compilation depend on preparation, adds the matching `nows-core` API, and can add GEB and Mixin compile/annotation-processor tooling to mod projects.

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
