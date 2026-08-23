## 0.6.2

- Published offline installer JARs for every supported Minecraft adapter so local release testing no longer depends on files being uploaded first.
- Added installer launcher targets for Official Launcher, Modrinth, CurseForge, Prism, MultiMC and generic instance folders.
- Added instance install support with `--launcher` and `--instanceDir`, including instance game-folder mod placement and a generated Nows version reference file.
- Added Prism/MultiMC instance launch metadata so those launchers run Nows through their component pipeline instead of only receiving copied files.
- Moved launcher and instance controls into the installer UI's advanced dialog so the main install flow stays focused on the Official Launcher path.
- Added JUnit coverage for installer launcher/instance option parsing and game-folder detection.

## 0.6.1

- Fixed the built-in Nows Mods title-screen button so Minecraft adapters reuse the active title screen instead of constructing a fresh `TitleScreen` during loader UI setup.
- Updated the Minecraft 26.2 title-screen hook for the newer client screen lifecycle while keeping the mod list's Done action parent-aware.
- Added a Nows-owned title-screen action for opening the built-in Nows Mods screen, with each Minecraft adapter binding it to that version's screen API.
- Aligned API mod Java release selection with the Minecraft adapters that require Java 21.
- Removed the old `example-mod` project from the monorepo.

## 0.6.0

- Added stable Nows-owned registry specs for item and block registration across Minecraft versions.
- Added `BlockMaterial` so mods can describe block material intent without depending on Minecraft's version-specific material APIs.
- Added stable Nows-owned wrapper values for text, vectors, item stacks, commands, datapack targets and NBT payloads.
- Extended config, keybind, player, UI text, command, datapack, event, NBT and registry APIs so mods can keep calling Nows MC the same way across Minecraft versions.
- Added a parent-aware `McText` config screen overload for registered config screen factories.
- Updated every published Minecraft adapter to translate stable registry specs into that version's registry, item property and block property APIs.
- Added shared JUnit contract coverage for the stable registry API surface across all supported Minecraft adapter modules.

## 0.5.0

- Added `repos/NowsRemapper` as the shared local remapping library for installer and Gradle tooling.
- Installer now prepares the profile client jar locally from the user's vanilla Minecraft client jar or Mojang's official downloads.
- Legacy obfuscated Minecraft clients are remapped locally with Mojang's official client mappings during install instead of being redistributed by Nows.

## 0.4.3

- Removed prepared Minecraft `client-dev.jar` artifacts from the hosted release layout and offline installer payload.

## 0.4.2

- Split the installer into a normal internet-first installer and a separate offline installer JAR.
- Offline installer JAR now embeds the generated release manifest, loader libraries, runtime dependencies and prepared Mojang-named `client-dev.jar`.
- Simplified the normal installer UI so players only choose the Nows/Minecraft version and Minecraft folder; advanced options now only cover the optional profile game folder.
- Published release layouts now include both installer artifacts under `releases/nows/<version>/installers/`.

## 0.4.1

- Fixed Official Launcher installs for versions such as Minecraft 1.20.1 by publishing the remapped `client/client-dev.jar` for every supported Minecraft target.
- Installer now writes `downloads.client` metadata for the prepared profile JAR so the Official Launcher validates the remapped JAR instead of replacing it with the vanilla client.
- Added client JAR SHA-256 metadata to generated release manifests.

## 0.4.0

- Introduced the modular 0.4 release layout for Nows loader artifacts, integrations, runtime and version-specific Minecraft adapters.
- Added the installer-driven Official Launcher profile flow using generated `install.properties` manifests.
- Published multi-version artifacts for the supported Minecraft targets and kept the Gradle plugin/API mod release outputs in the same release workflow.
