# Build and release notes

This document keeps installer, Gradle plugin and release commands out of the root README.

## Common commands

Fresh clone setup and full local validation:

```bash
./gradlew prepareWorkspace
```

Normal distribution build:

```bash
./gradlew dist
```

Optional single-JAR experiment:

```bash
./gradlew allJar
```

Stage release files for the default Minecraft target from `gradle.properties`:

```bash
./gradlew publishLayout
```

Stage a different supported Minecraft target:

```bash
./gradlew -Pminecraft_version=<minecraft-version> publishLayout
```

Build only the local Maven repository for mod developers:

```bash
./gradlew publishMavenLayout
```

Build/test the Gradle plugin:

```bash
./gradlew :repos:NowsGradlePlugin:build
```

Build the user-facing UI installer, CLI installer and offline installer:

```bash
./gradlew :repos:NowsInstaller:assemble
```

Build distribution artifacts inside Docker. This uses one Gradle worker by default and stores Gradle and Minecraft caches in Docker volumes:

```bash
docker compose run --rm nows-build
```

Build the signed upload layout inside Docker. This service mounts `${HOME}/.gnupg` so `publishMavenLayout` can use the normal GPG signing key:

```bash
docker compose run --rm nows-release
```

Open a container shell with the same toolchain and caches:

```bash
docker compose run --rm nows-shell
```

Run NowsWeb through Vite when the optional private checkout exists at `repos/NowsWeb`:

```bash
git submodule update --init repos/NowsWeb
docker compose up nows-web
```

## Version management

Update the coordinated project version:

```bash
./gradlew setNowsVersion -Pnew_nows_version=0.5.0
```

Inspect the coordinated version state:

```bash
./gradlew versionReport
```

`gradle.properties` is the source of truth for the monorepo's `nows_version`. Build logic generates installer defaults, runtime version resources, Gradle plugin defaults and release manifests from that value.

`repos/NowsApiMod` is also usable as a standalone repository, so it keeps its own `gradle.properties`. Use `setNowsVersion` to update both the monorepo and standalone companion mod state.

## Installer

`repos/NowsInstaller` is internet-first. It downloads a release manifest from:

```text
https://files.nows.space/releases/nows/<nows-version>/<minecraft-version>/install.properties
```

The installer writes Nows modules and libraries into the normal `.minecraft/libraries` tree, then generates an inherited Official Launcher version with `space.nows.mcnows.runtime.NowsLauncher` as the main class.

Existing launcher profiles are preserved and `launcher_profiles.json` is backed up before modification.

Default Minecraft directories:

- Windows: `%APPDATA%\.minecraft`
- Linux: `~/.minecraft`
- macOS: `~/Library/Application Support/minecraft`

The normal installed profile id is:

```text
nows-<nows-version>-<minecraft-version>
```

The installer does not set `gameDir` by default. The normal mod folder is therefore:

```text
.minecraft/mods
```

Nows also scans this optional profile overlay:

```text
.minecraft/nows/profiles/nows-<nows-version>-<minecraft-version>/mods
```

## Offline installer

The published offline installer is version-specific because it embeds `install.properties`, Nows modules and `nows-mc-<minecraft-version>` for one Minecraft version.

Expected output:

```text
.publishing/releases/nows/<nows-version>/<minecraft-version>/installers/NowsInstaller-offline-<nows-version>-mc-<minecraft-version>.jar
```

Local installer outputs:

```text
repos/NowsInstaller/build/libs/NowsInstaller-cli-<version>.jar
repos/NowsInstaller/build/libs/NowsInstaller-ui-<version>.jar
repos/NowsInstaller/build/libs/NowsInstaller-offline-<version>.jar
```

For local testing without downloads:

```bash
java -jar NowsInstaller-cli-<version>.jar --offline --manifest <local install.properties> --artifactDir <local library root>
```

The normal public download should point to the UI installer:

```text
.publishing/releases/nows/<nows-version>/<minecraft-version>/installers/NowsInstaller-ui-<nows-version>.jar
```

## KDL4J resolution rule

`com.github.kdl-org:kdl4j:v1.0.1` is resolved from JitPack first. GitHub Packages remains configured after JitPack for release environments that still need legacy fallback access. Installer builds resolve the original JAR once and store it inside installer JARs under:

```text
META-INF/nows/embedded-libs/kdl4j-v1.0.1.jar
```

At install time, the installer copies that untouched JAR to:

```text
.minecraft/libraries/com/github/kdl-org/kdl4j/v1.0.1/kdl4j-v1.0.1.jar
```

Players do not need `GITHUB_TOKEN` to install or run Nows.

Build credentials:

```properties
gpr.user=YOUR_GITHUB_USER
gpr.token=TOKEN_WITH_READ_PACKAGES
```

`gpr.key` is still accepted as a compatibility alias.

## Developer Maven layout

Generated release files are written under:

```text
.publishing/releases/nows/<nows-version>/<minecraft-version>/
```

Developer-facing Maven artifacts are written under:

```text
.publishing/maven/
```

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
```

Maven publishing always uses local GPG signing, even for `.publishing/maven/`. Make sure `gpg` has a usable default signing key before running `publishMavenLayout`.

## Gradle plugin

Plugin id:

```kotlin
plugins {
    id("space.nows.mcnows") version "0.4.0"
}

nows {
    minecraftVersion.set("26.2")
    nowsVersion.set("0.4.0")
}
```

The plugin owns Minecraft development setup rather than loader core. `nowsPrepareMinecraft` downloads the official client artifact and official `client_mappings` metadata. For modern unobfuscated Minecraft it detects Mojang-named classes and skips remapping. For older obfuscated versions it reads Mojang's ProGuard mapping file with Nows' own parser and remaps obfuscated names to official Mojang names with the Nows ASM remapper.

It applies Gradle's Java plugin, wires the prepared development client JAR into `compileOnly`, makes Java compilation depend on preparation, adds `nows-core`, the matching `nows-mc-<minecraft-version>` adapter, default KDL/logging/GEB/network/Mixin compile-time tooling, common Minecraft-owned compile libraries, and expands `${nowsVersion}` plus `${minecraftVersion}` in `nows.mod.kdl`.
