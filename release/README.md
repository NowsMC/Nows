# Nows release layout

Nows 0.3.x installs modularly. Upload the module JARs and `install.properties` to:

`https://files.nows.space/releases/nows/<nows-version>/<minecraft-version>/`

`repos/NowsInstaller/install.properties.template` documents the internet install protocol.
Before publishing, fill SHA-256 fields for every artifact. `dev.kdl:kdl4j` is intentionally **not** downloaded by the end user: its original JAR is embedded in the NowsInstaller CLI/UI/offline JARs at build time and extracted to the normal Minecraft library path. The offline installer additionally embeds every manifest artifact so local testing/installing does not require network access.

Use `./gradlew publishLayout` to build `.publishing/releases/nows/<nows-version>/<minecraft-version>/`.

To build a different Minecraft target, pass the Gradle property:

```bash
./gradlew -Pminecraft_version=1.20.1 publishLayout
```

The normal CLI/UI installer JARs are shared entrypoints. The offline installer in the publish layout is renamed with `-mc-<minecraft-version>` because it embeds the payload for exactly that game version.

The optional `:runtime:allJar` task is the future single-JAR distribution and is not the default installer payload.
