# Client UI And Player Helpers

Client UI helpers cover common cases: opening a screen, making a simple screen, adding title-screen buttons and drawing small overlays. Raw Minecraft UI objects remain exposed for deeper behavior.

```java
Ui ui = MinecraftApi.ui(context);
ui.titleScreen().addButton(title -> title.button(
        title.centerX(98), title.height() / 4 + 120, 98, 20,
        Component.literal("My Mod"),
        button -> ui.show(ui.simpleScreen(
                Component.literal("My Mod"),
                screen -> screen.addButton(screen.centerX(120), screen.height() - 40, 120, 20,
                        Component.literal("Done"), done -> ui.close()),
                render -> render.centeredText(Component.literal("Hello from Nows UI"),
                        render.width() / 2, 40, 0xFFFFFFFF)
        ))
));

ui.titleScreen().render(render ->
        render.text("my_mod loaded", 2, render.height() - 42, 0xFFAAFFAA));
```

Client player helpers expose both nullable and fail-fast player access. On multiplayer servers, server-authoritative values may be corrected by the server:

```java
PlayerApi player = MinecraftApi.player(context);
player.current().ifPresent(local -> {
    PlayerSnapshot snapshot = player.snapshot();
    player.sendOverlayMessage(Component.literal("Hello " + snapshot.name()));
    player.setHealth(Math.min(snapshot.maxHealth(), snapshot.health() + 2.0F));
    player.setFood(20);
    player.setSelectedHotbarSlot(0);
});
```
