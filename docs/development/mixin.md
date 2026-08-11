# Mixin

`integrations/mixin` provides Nows' own `IMixinService` and `IGlobalPropertyService`. The Mixin transformer is inserted directly into `NowsClassLoader` before class definition, including synthetic class generation. Fabric Loader is not required.
