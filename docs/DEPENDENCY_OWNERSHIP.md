# Dependency ownership

Nows uses a simple installation rule: **do not ship a second copy of a library when the Minecraft profile already owns a compatible copy.**

## Supplied by Minecraft / not installed again by Nows

- Log4j2
- SLF4J API and Log4j2 SLF4J binding
- Gson
- Guava
- JSpecify

These remain parent-first in `NowsClassLoader`.

## Supplied by Nows

- KDL4J 1.0.1 — embedded in `NowsInstaller.jar` because its primary release is GitHub Packages.
- GEB core 0.5.4 — internet install.
- Reactor Core 3.8.6 — internet install.
- Reactive Streams 1.0.4 — internet install.
- LMAX Disruptor 4.0.0 — internet install for Log4j2 async logging.
- Fabric Mixin 0.17.3+mixin.0.8.7 — internet install.
- ASM 9.8 modules required by Mixin — internet install.

`foo.zaaarf.geb:processor:0.4.9` is a build/annotation-processing dependency and is not installed into the player runtime.

The internet installer manifest is the final authority for player runtime delivery. This list documents the intended ownership policy; if Mojang changes its library set in a future Minecraft version, update the installer/runtime compatibility layer rather than `nows-core`.
