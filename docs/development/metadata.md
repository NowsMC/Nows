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

Mods can query loaded metadata through `NowsContext`:

```java
if (context.isModLoaded("other_mod")) {
    String name = context.requireModDescriptor("other_mod").name();
}

boolean clientRuntime = context.side() == NowsSide.CLIENT;
```
