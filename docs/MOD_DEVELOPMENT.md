# Mod development guide

Nows is a portability layer, not a full replacement for Minecraft APIs. Use the Nows helpers where they make cross-version work easier, and use Minecraft's own API directly for behavior that needs full control.

The mod-facing guide is split by topic under [`docs/development/`](development/):

- [Overview](development/README.md)
- [Metadata](development/metadata.md)
- [Lifecycle events](development/lifecycle.md)
- [Network channels](development/networking.md)
- [Minecraft adapter APIs](development/minecraft-adapters.md)
- [Data packs, commands and generated data](development/data-and-commands.md)
- [Config screens](development/config-screens.md)
- [Game events](development/game-events.md)
- [Client UI and player helpers](development/client-ui-and-player.md)
- [Config files](development/config-files.md)
- [Mixin](development/mixin.md)

## Known limitations

- Sending network messages does not work on any supported version, see [Network channels](development/networking.md).
- Dedicated server loading does not work. The loader has a server side and the installer can write a server folder, but the vanilla server jar does not start under it, see [Metadata](development/metadata.md).
- The player API, server and level tick callbacks, and block entity and menu registration are covered by no automated check. The checks that exist enter the main menu and never load a world.
