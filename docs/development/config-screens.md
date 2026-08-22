# Config Screens

Nows includes a small config screen layer for mods that only need common settings UI instead of depending directly on Cloth Config or a loader-specific mod menu integration.

Register a config screen factory during initialization:

```java
MinecraftApi.configUi(context).register("my_mod", parent ->
        MinecraftApi.configUi(context)
                .screen(McText.literal("My Mod Settings"))
                .category(McText.literal("General"))
                .booleanOption(
                        McText.translatable("option.my_mod.enabled"),
                        MyConfig.enabled(),
                        true,
                        McText.translatable("option.my_mod.enabled.tooltip"),
                        MyConfig::setEnabled)
                .intOption(
                        McText.translatable("option.my_mod.cooldown"),
                        MyConfig.cooldown(),
                        1000,
                        0,
                        60000,
                        McText.translatable("option.my_mod.cooldown.tooltip"),
                        MyConfig::setCooldown)
                .done()
                .saving(MyConfig::save)
                .build());
```

The built-in Nows mod list opens registered config screens from its Configure button. This covers the small, common Cloth Config pattern: category, boolean toggle, integer field, default/reset and save callback. `McText` is translated by each adapter into that Minecraft version's text/component API. More complex screens can still be custom Minecraft `Screen` classes returned by the registered factory.
