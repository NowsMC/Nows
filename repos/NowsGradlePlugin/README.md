# NowsGradlePlugin

Gradle plugin id:

```text
space.nows.mcnows
```

This project intentionally owns Minecraft/Gradle churn instead of `nows-core`.

It currently provides:

- `nowsPrepareMinecraft`;
- Mojang version metadata/client download;
- SHA-1 and size verification using Mojang metadata;
- direct use of already Mojang-named modern client JARs;
- legacy runtime-name -> official-Mojang-name remapping through Nows' own ProGuard-mapping parser and ASM remapper;
- automatic development Minecraft `compileOnly` classpath;
- Nows core API dependency;
- optional GEB compile/annotation-processor setup;
- optional Mixin compile/annotation-processor setup.

Example:

```kotlin
plugins {
    id("space.nows.mcnows") version "0.3.0"
}

nows {
    minecraftVersion.set("26.2")
    nowsVersion.set("0.3.0")
    officialMappings.set(true)
    developmentClientJar.set(layout.projectDirectory.file(".nows/minecraft/26.2/client-dev.jar"))
    addGeb.set(true)
    addMixin.set(true)
}
```
