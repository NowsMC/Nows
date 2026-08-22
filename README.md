# Nows MC

Nows MC is a Minecraft Java mod loader and stable wrapper layer for mod authors
who want the same Nows MC calls across supported game versions.

It sits between a mod and Minecraft so common mod-facing calls stay stable while
each `mc/<version>` adapter maps those calls onto that version's real game API.
Use Minecraft's own APIs directly whenever that is clearer; use Nows when the
wrapper boundary can remove repeated version glue.

Nows is experimental. It does not promise automatic porting, full API coverage or a
replacement for Minecraft APIs. The goal is practical portability: fewer repeated
files, smaller adapter changes and helpers that come from real mod development needs.

## Scope

Good fits for Nows:

- metadata and lifecycle setup
- stable registry specs for common content
- config screens, keybinds, tick callbacks and small client conveniences
- text, vectors, item stacks, commands, datapack targets, NBT, generated data,
  resources, recipe display metadata and networking surfaces

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
