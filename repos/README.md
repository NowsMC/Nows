# Tooling repositories

`repos/` contains projects intentionally kept outside the loader/runtime architecture because their compatibility surface changes faster than the loader kernel.

- `NowsInstaller/` — installs Nows into the Official Minecraft Launcher. Internet-first for now; GitHub Packages payloads can be embedded and extracted locally.
- `NowsGradlePlugin/` — mod-development plugin for Mojang mappings, Minecraft development JAR preparation, compile tooling and Gradle compatibility workarounds.
- `NowsWeb/` — web surface for docs, downloads, and release metadata. Kept as a git submodule because it uses a separate frontend toolchain.

They are included in this monorepo build today, but each directory is designed so it can become a standalone repository later without moving APIs into `nows-core`.
