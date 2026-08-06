# NowsInstaller

Internet-first Official Launcher installer for Nows.

The installer itself contains no loader architecture. It reads `install.properties` from `nows.space`, installs each declared artifact under `.minecraft/libraries`, then writes an inherited Launcher version profile whose `mainClass` is `space.nows.mcnows.runtime.NowsLauncher`.

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

Use `install.properties.template` as the release template. Release automation should fill SHA-256 values before publishing.
