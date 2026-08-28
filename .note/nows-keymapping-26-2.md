# Nows 26.2 KeybindApi crash with Minecraft KeyMapping.Category

Date: 2026-08-28

## Summary

`space.nows.mc.internal.client.keybind.KeybindApiImpl` in the 26.2 adapter still creates `net.minecraft.client.KeyMapping` with category as `String`.

Minecraft 26.2 no longer exposes these constructors:

```text
KeyMapping(String, InputConstants.Type, int, String)
KeyMapping(String, int, String)
```

The available constructors are:

```text
KeyMapping(String, int, KeyMapping.Category)
KeyMapping(String, InputConstants.Type, int, KeyMapping.Category)
KeyMapping(String, InputConstants.Type, int, KeyMapping.Category, int)
```

This makes `MinecraftApi.keybinds(context).registerKeyboard(...)` crash on the first registration.

## Reproducer

Kaleido originally called:

```java
MinecraftApi.keybinds(context).registerKeyboard(
        "key.kaleido.reload",
        "key.categories.kaleido",
        GLFW_KEY_F6,
        reloadAction
);
```

The game starts loading normally, runs Kaleido's entrypoint, then crashes on the render/client tick when the keybind registration is attempted.

## Error

```text
[19:34:07] [Render thread/ERROR]: Unreported exception thrown!
java.lang.IllegalStateException: Unsupported KeyMapping constructor
    at nows-game//space.nows.mc.internal.client.keybind.KeybindApiImpl.createKeyboardMapping(KeybindApiImpl.java:138)
    at nows-game//space.nows.mc.internal.client.keybind.KeybindApiImpl.registerKeyboard(KeybindApiImpl.java:74)
    at nows-game//top.kaleidoshade.client.ShaderControls.registerKeybinds(ShaderControls.java:55)
    at nows-game//top.kaleidoshade.client.ShaderControls.registerKeybindsWhenReady(ShaderControls.java:67)
    at nows-game//top.kaleidoshade.client.ShaderControls.lambda$initialize$0(ShaderControls.java:33)
    at nows-game//space.nows.mc.internal.event.GameEventsImpl.dispatchClientTick(GameEventsImpl.java:56)
    at nows-game//net.minecraft.client.Minecraft.handler$zzb000$nows$tickClient(Minecraft.java:3151)
    at nows-game//net.minecraft.client.Minecraft.tick(Minecraft.java:1956)
    at nows-game//net.minecraft.client.Minecraft.runTick(Minecraft.java:1250)
    at nows-game//net.minecraft.client.Minecraft.run(Minecraft.java:959)
    at nows-game//net.minecraft.client.main.Main.main(Main.java:292)
    at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
    at java.base/java.lang.reflect.Method.invoke(Method.java:565)
    at space.nows.loader.runtime.NowsLauncher.invokeMinecraftMain(NowsLauncher.java:424)
    at space.nows.loader.runtime.NowsLauncher.lambda$launch$21(NowsLauncher.java:194)
    at space.nows.loader.runtime.NowsLauncher.lambda$phase$0(NowsLauncher.java:572)
    at space.nows.loader.runtime.NowsLauncher.phase(NowsLauncher.java:559)
    at space.nows.loader.runtime.NowsLauncher.phase(NowsLauncher.java:571)
    at space.nows.loader.runtime.NowsLauncher.launch(NowsLauncher.java:193)
    at space.nows.loader.runtime.NowsLauncher.main(NowsLauncher.java:84)
Caused by: java.lang.NoSuchMethodException: net.minecraft.client.KeyMapping.<init>(java.lang.String,int,java.lang.String)
    at java.base/java.lang.Class.getConstructor0(Class.java:3187)
    at java.base/java.lang.Class.getConstructor(Class.java:2199)
    at nows-game//space.nows.mc.internal.client.keybind.KeybindApiImpl.createKeyboardMapping(KeybindApiImpl.java:135)
    ... 19 more
```

## Likely fix

For Minecraft 26.2, `KeybindApiImpl.createKeyboardMapping` should create or reuse a `KeyMapping.Category` and pass that to the constructor instead of a raw category string.

The category API in 26.2 is:

```java
KeyMapping.Category.register(Identifier id)
```

The category label is resolved with:

```text
Identifier.toLanguageKey("key.category")
```

So a category id such as `kaleidoshade:kaleido` resolves to:

```text
key.category.kaleidoshade.kaleido
```

Kaleido currently works around this locally by registering key mappings directly against Minecraft 26.2 and continuing to use Nows for `clientTick`, `ui`, and `configUi`.
