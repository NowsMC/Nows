# Metadata

KDL is the recommended human-facing metadata format today, but it is replaceable. `integrations/kdl` turns `nows.mod.kdl` into the generic `ModDescriptor` model owned by `core`.

```kdl
mod id="my_mod" name="My Mod" version="1.0.0" minecraft="26.2" side="client" {
    info {
        description "Short description shown to tools and companion UI."
        author "YourName"
        license "Apache-2.0"
        icon "assets/my_mod/icon.png"
    }

    links {
        homepage "https://example.com"
        sources "https://github.com/example/my-mod"
    }

    compatibility {
        requires "minecraft" version="26.2"
        depends "cloth-config" version=">=11.0.0"
        recommends "modmenu" version=">=1.0.0"
        incompatible-with "bad_mod" reason="Breaks the same screen"
    }

    load-order {
        after "cloth-config"
        before "late_mod"
    }

    properties {
        channel "stable"
    }

    runtime {
        network-channel "my_mod:main"
        listener "com.example.MyLifecycleListener"
        entrypoint "com.example.MyMod"
        transformer "com.example.MyTransformer"
        mixin "my_mod.mixins.json"
    }
}
```

Grouped KDL is the recommended style, but equivalent flat nodes remain supported. Runtime-provided ids such as `minecraft`, `nows` and `nows-loader` can be used in dependency declarations.

`side` accepts `client`, `server`, `both` or `common`. The current launcher runtime is client-side and rejects server-only mods before loading mod classes.

Dedicated servers do not work yet. The launcher accepts `--nowsSide server` and the installer's `--server` option writes a server folder, but the vanilla server jar does not start under it. With the classpath those scripts build (the Nows libraries and the vanilla server jar) the loader stops at `Install Mixin integration` with a `NoClassDefFoundError` for Guava. With Guava and Log4j added it stops at `Invoke Minecraft main`, because `net.minecraft.server.Main` is inside the vanilla bundler jar and the loader does not unpack it. This was seen on 1.20.1 and 26.2 before the server's EULA step, which is left to the person running the server and was never accepted by a test. The Minecraft adapter is skipped on the server side, so no `MinecraftApi` call is available there. `scripts/verify.ps1` does not start a server.

Mods can query loaded metadata through `NowsContext`:

```java
if (context.isModLoaded("other_mod")) {
    String name = context.requireModDescriptor("other_mod").name();
}

boolean clientRuntime = context.side() == NowsSide.CLIENT;
```
