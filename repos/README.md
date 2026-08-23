# Tooling repositories

`repos/` contains projects intentionally kept outside the loader/runtime architecture because their compatibility surface changes faster than the loader kernel.

- `NowsInstaller/` — installs Nows into the Official Minecraft Launcher. Internet-first for now; GitHub Packages payloads can be embedded and extracted locally.
- `NowsRemapper/` — shared local Minecraft JAR remapper used by installer and Gradle tooling. It prepares Mojang-named jars from the user's local/official Minecraft downloads without publishing Minecraft client jars.
- `NowsGradlePlugin/` — mod-development plugin for Mojang mappings, Minecraft development JAR preparation, compile tooling and Gradle compatibility workarounds.
- `NowsApiMod/` — optional companion mod and local fixture for Nows Gradle plugin, KDL metadata, Mixin and version-specific Minecraft API adapters.
- `NowsMCreatorGenerator/` — MCreator generator integration for producing Nows projects. Branches are split by target Minecraft version; the first local branch is `26.2`.
- `NowsWeb/` — optional private submodule for docs, downloads, and release metadata. It is visible here so contributors know it exists, but it is not required for loader, installer, Gradle plugin or adapter development. Contributors with repository access can fetch it with `git submodule update --init repos/NowsWeb`.

The required directories are included in this monorepo build today, but each directory is designed so it can become a standalone repository later without moving APIs into `nows-core`.
