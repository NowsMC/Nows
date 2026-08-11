# Config Screens

Nows includes a small config screen layer for mods that only need common settings UI instead of depending directly on Cloth Config or a loader-specific mod menu integration.

Register a config screen factory during initialization:

```java
MinecraftApi.configUi(context).register("my_mod", parent ->
        MinecraftApi.configUi(context)
                .screen(parent, Component.literal("My Mod Settings"))
                .category(Component.literal("General"))
                .booleanOption(
                        Component.literal("Enable Feature"),
                        MyConfig.enabled(),
                        true,
                        Component.literal("Turn the main feature on or off."),
                        MyConfig::setEnabled)
                .intOption(
                        Component.literal("Cooldown"),
                        MyConfig.cooldown(),
                        1000,
                        0,
                        60000,
                        Component.literal("Cooldown in milliseconds."),
                        MyConfig::setCooldown)
                .done()
                .saving(MyConfig::save)
                .build());
```

The built-in Nows mod list opens registered config screens from its Configure button. This covers the small, common Cloth Config pattern: category, boolean toggle, integer field, default/reset and save callback. More complex screens can still be custom Minecraft `Screen` classes returned by the registered factory.
