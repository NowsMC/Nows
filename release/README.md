# Nows release layout

Nows 0.4.x installs modularly. Upload the module JARs and `install.properties` to:

`https://files.nows.space/releases/nows/<nows-version>/<minecraft-version>/`

`repos/NowsInstaller/install.properties.template` documents the internet install protocol.
Before publishing, fill SHA-256 fields for every artifact. `com.github.kdl-org:kdl4j` is intentionally **not** downloaded by the end user: its original JAR is resolved from JitPack first, embedded in the NowsInstaller CLI/UI/offline JARs at build time, and extracted to the normal Minecraft library path. The offline installer additionally embeds every manifest artifact so local testing/installing does not require network access.

Use `./gradlew publishLayout` to build `.publishing/releases/nows/<nows-version>/<minecraft-version>/` and `.publishing/maven/`.

Docker release build:

```bash
docker compose run --rm nows-release
```

For a fresh clone intended for local testing, use the broader setup task:

```bash
./gradlew prepareWorkspace
```

That task syncs submodules before checking tools and building artifacts.

To build a different Minecraft target, pass the Gradle property:

```bash
./gradlew -Pminecraft_version=1.20.1 publishLayout
```

The normal CLI/UI installer JARs are shared entrypoints. The offline installer in the publish layout is renamed with `-mc-<minecraft-version>` because it embeds the payload for exactly that game version. `mods/nows-api-mod-<version>-mc-<minecraft-version>.jar` is staged as an optional companion mod artifact; it is not required for the built-in title-screen Nows badge.

Upload `.publishing/maven/` to the Maven repository path, expected as `https://files.nows.space/maven/`. This repo is for mod developers and publishes Nows APIs, Minecraft-version adapters, integrations, runtime and the Gradle plugin marker. POM metadata uses Apache License 2.0 and lists `TamKungZ_` as maintainer and `HollZaterQ` as tester.

Every Maven publication is signed with local GPG before it is written to `.publishing/maven/`. A release build must fail rather than publish unsigned Maven artifacts.

The optional `:runtime:allJar` task is the future single-JAR distribution and is not the default installer payload.
