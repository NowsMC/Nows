# Nows 0.10.0 KeybindApi custom category duplicate crash

Date: 2026-08-29

## Summary

Nows 0.10.0 fixes the Minecraft 26.2 `KeyMapping` constructor shape by using `KeyMapping.Category`, but custom keybind categories can still crash.

`KeybindApiImpl.registerKeyboard(id, category, key, onPress)` calls `registerCategory(category)` first. In Nows 0.10.0, `registerCategory` creates and stores a Minecraft `KeyMapping.Category`. After that, `createKeyboardMapping(id, category, key)` calls `category(category)` again, and that calls `KeyMapping.Category.register(id)` a second time.

Minecraft 26.2 throws when a category id is already registered.

## Error

```text
[02:38:24] [Render thread/ERROR]: Unreported exception thrown!
java.lang.IllegalArgumentException: Category 'kaleidoshade:kaleido' is already registered.
    at nows-game//net.minecraft.client.KeyMapping$Category.register(KeyMapping.java:43)
    at nows-game//space.nows.mc.internal.client.keybind.KeybindApiImpl.category(KeybindApiImpl.java:151)
    at nows-game//space.nows.mc.internal.client.keybind.KeybindApiImpl.createKeyboardMapping(KeybindApiImpl.java:125)
    at nows-game//space.nows.mc.internal.client.keybind.KeybindApiImpl.registerKeyboard(KeybindApiImpl.java:74)
    at nows-game//top.kaleidoshade.client.ShaderControls.registerKeybinds(ShaderControls.java:64)
    at nows-game//top.kaleidoshade.client.ShaderControls.registerKeybindsWhenReady(ShaderControls.java:76)
    at nows-game//top.kaleidoshade.client.ShaderControls.lambda$initialize$0(ShaderControls.java:35)
    at nows-game//space.nows.mc.internal.event.GameEventsImpl.dispatchClientTick(GameEventsImpl.java:56)
    at nows-game//net.minecraft.client.Minecraft.handler$zzb000$nows$tickClient(Minecraft.java:3151)
    at nows-game//net.minecraft.client.Minecraft.tick(Minecraft.java:1956)
    at nows-game//net.minecraft.client.Minecraft.runTick(Minecraft.java:1250)
    at nows-game//net.minecraft.client.Minecraft.run(Minecraft.java:959)
    at nows-game//net.minecraft.client.main.Main.main(Main.java:292)
```

## Likely fix

`createKeyboardMapping` should reuse the already stored category from `categories`, or `category(String)` should return an existing category when it has already been registered.

One possible shape:

```java
registerCategory(category);
KeyMapping.Category minecraftCategory = categories.get(category);
KeyMapping mapping = createKeyboardMapping(id, minecraftCategory, glfwKeyCode);
```

Kaleido temporarily avoids `MinecraftApi.keybinds(...)` again and exposes its shader screen from Video Settings until this path is stable.
