# NowsInstaller

Internet-first Official Launcher installer for Nows, with a local offline mode for development testing.

The installer itself contains no loader architecture. It reads `install.properties` from `nows.space`, installs each declared artifact under `.minecraft/libraries`, then writes an inherited Launcher version profile whose `mainClass` is `space.nows.mcnows.runtime.NowsLauncher`.

The installer is compiled with Java 8 bytecode so it can run on Java 8 and newer.

## Build outputs

```bash
./gradlew :repos:NowsInstaller:assemble
```

This builds separate installer JARs:

```text
build/libs/NowsInstaller-cli-<version>.jar
build/libs/NowsInstaller-ui-<version>.jar
build/libs/NowsInstaller-offline-<version>.jar
```

Run the CLI installer with:

```bash
java -jar build/libs/NowsInstaller-cli-<version>.jar
```

Run the Swing UI installer with:

```bash
java -jar build/libs/NowsInstaller-ui-<version>.jar
```

Run the fully offline installer with:

```bash
java -jar build/libs/NowsInstaller-offline-<version>.jar
```

## GitHub Packages

GitHub Package dependencies are resolved when building the installer and embedded as untouched JAR resources. Currently:

```text
dev.kdl:kdl4j:1.0.1
 -> META-INF/nows/embedded-libs/kdl4j-1.0.1.jar
 -> .minecraft/libraries/dev/kdl/kdl4j/1.0.1/kdl4j-1.0.1.jar
```

End users therefore need no GitHub credentials. The release build does.

## Internet manifest

Default URL:

```text
https://nows.space/releases/nows/<nows-version>/install.properties
```

Use `install.properties.template` as the release template. Release automation should fill SHA-256 values before publishing. For `source=internet` artifacts, the normal installer first tries `artifact.<n>.url`; if that download fails or the URL is missing, it falls back to `artifact.<n>.mavenUrl` or `mavenBaseUrl + artifact.<n>.path`.

## Fully offline installer

`NowsInstaller-offline-<version>.jar` embeds the release manifest plus every non-Minecraft artifact listed by the installer manifest. Its Nows module payloads are taken from this workspace's Gradle project JAR tasks (`:core:jar`, `:runtime:jar`, integrations, and Minecraft support), so a local build installs the local project output. At install time it copies those embedded payloads into `.minecraft/libraries` and writes the same launcher profile as the normal installer without contacting `nows.space` or Maven.

## Offline local testing

Use `--offline` to install from files already on disk. In offline mode the installer never downloads the manifest or artifacts:

```bash
java -jar build/libs/NowsInstaller-cli-<version>.jar \
  --offline \
  --manifest /path/to/install.properties \
  --artifactDir /path/to/offline-libraries \
  --minecraftDir /path/to/test-minecraft
```

The local `install.properties` still uses the normal `artifact.*.path` values. For artifacts whose source is `internet`, place each JAR under `--artifactDir` using that same relative path, for example:

```text
/path/to/offline-libraries/space/nows/mcnows/nows-core/0.3.0/nows-core-0.3.0.jar
```

Artifacts marked `source=embedded` are still copied from inside the installer JAR. A manifest may also set `artifact.<n>.source=local` and `artifact.<n>.file=relative/or/absolute.jar` for one-off local test jars.
