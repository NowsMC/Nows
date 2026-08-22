## 0.6.0

- Added stable Nows-owned registry specs for item and block registration across Minecraft versions.
- Added `BlockMaterial` so mods can describe block material intent without depending on Minecraft's version-specific material APIs.
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
