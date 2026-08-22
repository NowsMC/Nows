# Nows MC

Nows MC is a small Minecraft Java mod loader and portability layer for mod
authors who want thinner version adapter code.

It sits between a mod and Minecraft so common loader and adapter code can change
less when the same mod moves across supported game versions. The layer is meant to
stay thin: use Nows for repeated setup work, then use Minecraft's own APIs directly
whenever that is clearer.

Nows is experimental. It does not promise automatic porting, full API coverage or a
replacement for Minecraft APIs. The goal is practical portability: fewer repeated
files, smaller adapter changes and helpers that come from real mod development needs.

## Scope

Good fits for Nows:

- metadata and lifecycle setup
- registry helpers for common content
- config screens, keybinds, tick callbacks and small client conveniences
- text, NBT, generated data, resources, recipe display metadata and networking
  surfaces

Use Minecraft classes, mixins or version-specific hooks directly when they are the
clearest tool.

## Links

- Website: <https://nows.space>
- Source: <https://github.com/NowsMC/Nows>
- Issues: <https://github.com/NowsMC/Nows/issues>

## Development Docs

- [Mod development guide](docs/MOD_DEVELOPMENT.md)
- [Architecture](docs/ARCHITECTURE.md)

## License

Apache License 2.0. See [LICENSE](LICENSE).

Maintained by TamKungZ_.
