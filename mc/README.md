# Minecraft version adapters

This directory owns version-specific code that talks directly to Minecraft APIs.

Use `mc/<minecraft-version>/` for API surfaces that depend on the exact Minecraft version, such as block registration, client UI/screen helpers, mod-menu hooks or future game integration points. Keep those direct `net.minecraft.*` references out of `core/`, `runtime/` and generic integrations.

Each version directory can contain:

- Java sources for the version-specific adapter artifact, for example `nows-mc-26.2`.
- Public API classes with stable Nows names, such as registry/datapack helpers, backed by that version's `net.minecraft.*` classes.
- `nows-minecraft.properties`, packaged by the generic `minecraft` module into `META-INF/nows/mc/`, for small launch-policy facts.

Only one `nows-mc-<minecraft-version>` artifact should be installed for a launcher profile.

Runtime installs `space.nows.mcnows.mc.internal.NowsMinecraftIntegration` when it is present. Mods should normally reach the services through `NowsMinecraft.registries(context)` and `NowsMinecraft.dataPacks(context)` instead of depending on runtime reflection details.

Minecraft versions that do not ship Mojang-named client jars, such as 1.20.1, need a remapped `.nows/minecraft/<version>/client-dev.jar` before their adapter module can compile.

Do not add `net.minecraft.` to parent-first classloading. Minecraft classes belong to the game classloader and should come from the selected vanilla client jar/inherited Launcher profile.
