# Config Files

Basic per-mod config files live in core because they are not Minecraft-version-specific:

```java
Properties config = context.configs().loadProperties("my_mod", "client");
context.configs().saveProperties("my_mod", "client", config, "My Mod client config");
```

Runtime installs these APIs through `NowsServices` when a matching `nows-mc-<version>` adapter is present. `core` stays free of `net.minecraft.*` types.
