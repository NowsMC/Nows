# Mixin

`integrations/mixin` provides Nows' own `IMixinService` and `IGlobalPropertyService`. The Mixin transformer is inserted directly into `NowsClassLoader` before class definition, including synthetic class generation. Fabric Loader is not required.

Declare Mixin configs from `nows.mod.kdl`:

```kdl
mixin "example.mixins.json"
```

Nows validates every declared config before registration. Missing or blank configs fail during loader bootstrap with the owning mod id and JAR path in the error, while duplicate config declarations are ignored after the first registration and logged as warnings. Built-in Minecraft adapter configs are registered before mod configs so loader UI/resource hooks are available early.

During startup, Mixin registration is also reported to `NowsLoadingState`, so the loading screen shows which built-in or mod-declared config is currently being processed.
