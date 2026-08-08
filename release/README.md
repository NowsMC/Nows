# Nows release layout

Nows 0.3.x installs modularly. Upload the module JARs and `install.properties` to:

`https://nows.space/releases/nows/<nows-version>/`

`repos/NowsInstaller/install.properties.template` documents the internet install protocol.
Before publishing, fill SHA-256 fields for every artifact. `dev.kdl:kdl4j` is intentionally **not** downloaded by the end user: its original JAR is embedded in the NowsInstaller CLI/UI/offline/dev-offline JARs at build time and extracted to the normal Minecraft library path. The offline installers additionally embed every manifest artifact so local testing/installing does not require network access.

The optional `:runtime:allJar` task is the future single-JAR distribution and is not the default installer payload.
