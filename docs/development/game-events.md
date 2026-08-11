# Game Events

Version adapters expose lightweight game callbacks for common Fabric/Forge event patterns:

```java
MinecraftApi.events(context).clientTick(minecraft -> {
    // Poll keybinds or update client-only helpers.
});

MinecraftApi.events(context).serverTick(server -> {
    // Run server-wide maintenance.
});

MinecraftApi.events(context).serverLevelTick((server, level) -> {
    // Run per-level managers such as spell effects or delayed block changes.
});
```

This is intentionally small. Use it for simple tick-driven managers like cooldown cleanup, gradual world effects or floating projectile state. Detailed player interaction and networking still belong in the dedicated API surfaces as they grow.
