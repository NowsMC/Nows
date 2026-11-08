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

Stage release files for every supported Minecraft adapter:

```bash
./gradlew publishLayout
```

Build only the local Maven repository for mod developers:

```bash
./gradlew publishMavenLayout
```

`publishMavenLayout` updates `.publishing/maven` in place. It overwrites files for the same coordinates but keeps older published versions in the local Maven tree.

Build/test the Gradle plugin:

```bash
./gradlew :repos:NowsGradlePlugin:build
```

Build the user-facing installer:

```bash
./gradlew :repos:NowsInstaller:assemble
```

Build all offline installers without staging the full release layout:

```bash
./gradlew :repos:NowsInstaller:allOfflineJars
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
./gradlew setNowsVersion -Pnew_nows_version=0.10.0
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

The installer writes Nows modules and libraries into the normal `.minecraft/libraries` tree, then generates an inherited Official Launcher version with `space.nows.loader.runtime.NowsLauncher` as the main class.

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

For launcher-managed instances, pass `--launcher <modrinth|curseforge|prism|multimc|generic>` and `--instanceDir <path>`. Official installs update `launcher_profiles.json`. Prism and MultiMC installs write instance pack metadata (`mmc-pack.json` plus `patches/space.nows.json`) so the launcher can run Nows directly. Modrinth, CurseForge and generic instance installs write mods into the selected instance game folder and write `nows/launcher-profile.properties` with the generated Nows version id; native app launch support still depends on that launcher accepting custom Minecraft version ids.

The generated launcher metadata keeps `space.nows:nows-mc-*` adapters out of launcher-owned libraries and passes `-Dnows.minecraftAdapterPath=<adapter.jar>` instead. That adapter must be loaded by Nows' game classloader, not by the launcher classpath, because it imports `net.minecraft.*` classes directly.

## Installer

`publishLayout` stages release folders for every supported `mc/<minecraft-version>` adapter:

```text
.publishing/releases/nows/<nows-version>/<minecraft-version>/
```

Each staged Minecraft version contains libraries, a processed `install.properties`, `SHA256SUMS`, and `mods/nows-api-mod-<nows-version>-mc-<minecraft-version>.jar` as an optional companion mod artifact. Minecraft client JARs are not staged or uploaded by Nows; the installer prepares the local profile client from the user's vanilla install or Mojang's official services.

Expected output:

```text
.publishing/releases/nows/<nows-version>/installers/NowsInstaller-<nows-version>.jar
.publishing/releases/nows/<nows-version>/installers/NowsInstallerOffline-<nows-version>-mc-<minecraft-version>.jar
```

Local installer output:

```text
repos/NowsInstaller/build/libs/NowsInstaller-<version>.jar
repos/NowsInstaller/build/libs/NowsInstallerOffline-<version>-mc-<minecraft-version>.jar
```

For local testing without downloads:

```bash
java -cp NowsInstaller-<version>.jar space.nows.installer.NowsInstaller --offline --manifest <local install.properties> --artifactDir <local library root>
```

The normal public download should point to the shared installer:

```text
.publishing/releases/nows/<nows-version>/installers/NowsInstaller-<nows-version>.jar
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

Maven publishing always uses local GPG signing, even for `.publishing/maven/`. Make sure `gpg` has a usable default signing key before running `publishMavenLayout`. The local Maven tree is updated in place so older versions remain available unless you delete them manually.

## Gradle plugin

Plugin id:

```kotlin
plugins {
    id("space.nows.gradle") version "0.10.0"
}

nows {
    minecraftVersion.set("26.2")
    nowsVersion.set("0.10.0")
}
```

The plugin owns Minecraft development setup rather than loader core. `nowsPrepareMinecraft` downloads the official client artifact and official `client_mappings` metadata. For modern unobfuscated Minecraft it detects Mojang-named classes and skips remapping. For older obfuscated versions it uses `repos/NowsRemapper` to read Mojang's ProGuard mapping file and remap obfuscated names to official Mojang names locally.

It applies Gradle's Java plugin, wires the prepared development client JAR into `compileOnly`, makes Java compilation depend on preparation, adds `nows-core`, the matching `nows-mc-<minecraft-version>` adapter, default KDL/logging/GEB/network/Mixin compile-time tooling, common Minecraft-owned compile libraries, and expands `${nowsVersion}` plus `${minecraftVersion}` in `nows.mod.kdl`.

## Runtime verification

A passing build does not show that the loader starts or that an outside mod can use the API. One script checks that on every `mc/<version>` adapter:

```powershell
pwsh scripts/verify.ps1 -JdkRoots G:\Java
```

For each adapter it runs `build check`, inspects the produced jars (policy and mixin configs, service files, doubled or unremapped classes, jars older than their sources), builds the consumer mod in `fixtures/consumer` through the Gradle plugin, starts the real client with that mod and waits for the main menu and the fixture's summary line. It then scans the full logs for failure patterns, compares the API results between adapters and, with `-BaselineFile`, the startup time against an earlier run. It exits non-zero on any failure, a missing summary line, a hang, a failure-pattern hit or a stale jar.

Requirements: PowerShell 7, a Java 25 JDK for Gradle (`-GradleJdk` or `JAVA_HOME`), the JDKs Minecraft needs for each adapter under `-JdkRoots` (17, 21 and 25 cover the supported versions), network access for Mojang's launcher metadata, libraries and assets (cached in `.nows/smoke`), and a display. Launching is implemented for Windows x64. No GPG key is needed.

Client launches use the dummy token `--accessToken 0`, so Mojang's online services log a fixed set of authentication and Realms errors. The script allows exactly those log entries, matched by their full message, and nothing else. Use `-NoMod -WriteBaselineFile baseline.json` on another checkout to record startup times without the consumer mod.
