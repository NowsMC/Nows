## 0.9.3

- Expanded Minecraft registry adapter coverage across every supported `mc/<version>` artifact with stable Nows entrypoints for entity types, mob effects, potions, attributes, particle types, fluids, bucket items, fixed-range sounds and villager professions.
- Added version-specific registry helpers where Minecraft exposes matching built-in registries, including enchantments on older adapters and painting variants/banner patterns on adapters that still own those registries directly.
- Updated the Minecraft adapter policy docs to define `mc/` as the stable Nows-owned wrapper/adapter layer over version-specific Minecraft APIs.
- Bumped Nows release metadata to 0.9.3.

## 0.9.2

- Hardened loader dependency version matching for SemVer prereleases, build metadata, Windows/.NET versions, Java update versions, CalVer, serial versions, codenames, commit hashes and loose development/build labels.
- Added side-aware runtime launch handling so server launches use the server main class, skip client hooks/UI and keep client-only built-in mixin configs off dedicated server runs.
- Added NowsInstaller dedicated-server installs that generate runnable `run-nows-server.sh` and `run-nows-server.bat` scripts, server config folders and an EULA placeholder without requiring a client JAR.
- Changed `publishMavenLayout` so `.publishing/maven` is updated in place instead of deleting older local Maven versions first.
- Added wildcard equality matching for dependency constraints such as `1.2.x` and `1.2.*`.
- Added JUnit coverage for the broader dependency version forms and deterministic prerelease/build label ordering.
- Hardened stable Minecraft wrapper/spec validation so bad resource ids, empty recipes and null recipe/data parts fail in Nows shared APIs before reaching version adapters.
- Added explicit service registry replacement/optional-registration helpers for integrations that need a controlled lifecycle.
- Improved Mixin bootstrap observability with loading-screen progress for built-in and mod-declared config registration.
- Reworked the Minecraft loading overlay so vanilla keeps its game loading bar while Nows renders a separate stable-position progress bar for loader phases, mod entrypoints, Mixin configs and resource packs.
- Improved large-mod-set startup reporting with discovery progress, skipped non-Nows jar reporting and declaration summaries instead of unbounded per-mod log spam.
- Hardened generated dedicated-server scripts so they run from the installed server directory, check for the Minecraft server JAR, require `eula=true` before launch and include a server README with the installed files and first-run steps.
- Bumped Nows release metadata to 0.9.2.

## 0.9.1

- Added shared Nows-owned bridges for converting `McText` and `McItemStack` into the selected Minecraft adapter's native component and item stack types.
- Centralized repeated `McText` conversion across text, config and player APIs so stable text wrappers stay the common path while native component overloads remain available as escape hatches.
- Added stable recipe-viewer overloads for category titles, icons, catalysts and common slot builders using `McText` and `McItemStack`.
- Added stable container layout, screen and workstation recipe specs for slots, synced data, progress bars and custom cooking recipe shapes.
- Hardened client resource-pack repository hooks by pinning modern `PackRepository` constructor descriptors and detecting `ClientPackSource` by type instead of exact class-name equality.
- Added a lightweight loading diagnostics overlay with heap/offheap/CPU stats and Nows phase text while keeping Minecraft's splash screen and main progress bar vanilla-owned.
- Bumped Nows release metadata to 0.9.1.

## 0.9.0

- Added broader Nows-owned Minecraft wrapper APIs for identifiers, positions, directions, item stacks, data components, recipes, tags, command contexts and event contexts.
- Extended stable item and block specs with common generator-facing properties such as food, tools, armor, rarity, durability, block sound, light, render type, shape and movement factors.
- Updated every supported Minecraft adapter to expose the new stable overloads and translate common spec fields without requiring generated code to import Minecraft classes.
- Updated the Nows MCreator generator templates to prefer stable Nows APIs for item stacks, generated tags, food items and block sound/light declarations.
- Bumped Nows release metadata to 0.9.0.

## 0.8.0

- Renamed public Java packages into clearer Nows namespaces.
- Hardened core loader contracts with stricter context, service, config, classloader, mod metadata, discovery and dependency validation.
- Hardened integration contracts for KDL metadata parsing, GEB listener registration, logging setup, Mixin properties and network channel handling.
- Bumped Nows release metadata to 0.8.0.

## 0.7.0

- Published offline installer JARs for every supported Minecraft adapter so local release testing no longer depends on files being uploaded first.
- Added installer launcher targets for Official Launcher, Modrinth, CurseForge, Prism, MultiMC and generic instance folders.
- Added instance install support with `--launcher` and `--instanceDir`, including instance game-folder mod placement and a generated Nows version reference file.
- Added Prism/MultiMC instance launch metadata so those launchers run Nows through their component pipeline instead of only receiving copied files.
- Moved launcher and instance controls into the installer UI's advanced dialog so the main install flow stays focused on the Official Launcher path.
- Offline installer JARs now open the same UI as the normal installer, with the bundled Minecraft version locked to the selected offline package.
- Official Launcher profiles now request the Java runtime required by Nows and the target Minecraft version instead of inheriting legacy Java 8 from older vanilla profiles.
- Offline installs now fail clearly when no local Minecraft client JAR is available to prepare the Nows profile, instead of leaving a launcher profile that later runs the obfuscated vanilla client and crashes.
- Fixed the local Minecraft remapper so inherited/static method call sites are remapped with the same owner/descriptor namespace as their declarations, preventing startup crashes such as `V705.a()` and `Block.b(...)` on remapped clients.
- Gradle and installer Minecraft preparation now rebuild from the runtime client plus cached Mojang mappings before trusting an existing profile JAR, so stale or manually copied remapped clients do not keep breaking every launcher profile.
- Added remapper fingerprint metadata to prepared Minecraft client jars so repeat Gradle and installer runs reuse valid remapped clients instead of remapping every Minecraft version again.
- Kept version-specific `nows-mc-*` adapters off launcher-owned classpaths and passed them to the Nows game classloader by path, fixing Minecraft verifier crashes on startup.
- Kept Minecraft-owned packages child-only inside the Nows game classloader so failed game-class transforms do not fall back to the launcher classpath and hide the real adapter error.
- Fixed legacy `PackRepository` mixin constructor targeting for Minecraft 1.16.4 through 1.19.2, preventing resource-pack source injection from crashing on startup.
- Fixed Nows mod resource-pack metadata and pack formats for Minecraft 1.16.4 through 1.21.1 so loader resources create valid packs instead of `null` entries.
- Moved the Minecraft 1.21.11 title-screen integration off `TitleScreen` itself and onto loader-owned init/render hooks so the latest client reaches the title screen cleanly while still showing the Nows Mods action.
- Fixed the Minecraft 1.21.11 Nows Mods screen so opening it does not request a second GUI blur pass and crash while rendering.
- Fixed the Minecraft 1.21.11 title-screen Nows Mods button rendering so the icon keeps the normal vanilla button background.
- Added JUnit coverage for installer launcher/instance option parsing, game-folder detection, adapter classpath isolation and per-version title-screen hook strategies.
- Added shared Minecraft adapter JUnit contracts for legacy `PackRepository` constructor hooks and Nows resource-pack metadata.

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
