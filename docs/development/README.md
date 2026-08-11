# Mod Development

These notes are for mod authors targeting Nows. The APIs here are deliberately small: they reduce common porting work between supported Minecraft major versions, while leaving full-detail behavior available through normal Minecraft classes.

## Topics

- [Metadata](metadata.md) - `nows.mod.kdl`, dependencies, side declarations and loaded mod metadata.
- [Lifecycle events](lifecycle.md) - loader lifecycle listeners and GEB integration.
- [Network channels](networking.md) - Nows network channel declarations and packet handlers.
- [Minecraft adapter APIs](minecraft-adapters.md) - registry helpers, text/NBT helpers, keybinds, recipe display metadata and simple block bases.
- [Data packs, commands and generated data](data-and-commands.md) - pack sources, command collectors and JSON output.
- [Config screens](config-screens.md) - small Cloth Config-like screens.
- [Game events](game-events.md) - lightweight tick callbacks.
- [Client UI and player helpers](client-ui-and-player.md) - title-screen UI, simple screens, keybind callbacks and local player helpers.
- [Config files](config-files.md) - Minecraft-neutral per-mod config storage.
- [Mixin](mixin.md) - Nows Mixin transformer support.
