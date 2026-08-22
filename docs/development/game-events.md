# Game Events

Version adapters expose lightweight game callbacks for common Fabric/Forge event patterns:

```java
MinecraftApi.events(context).clientTick(() -> {
    // Run version-neutral client work.
});

MinecraftApi.events(context).clientTick(minecraft -> {
    // Drop down to the native Minecraft client when needed.
});

MinecraftApi.events(context).serverTick(() -> {
    // Run server-wide maintenance without depending on the server class.
});

MinecraftApi.events(context).serverLevelTick((server, level) -> {
    // Run per-level managers such as spell effects or delayed block changes.
});
```

This is intentionally small. Use the `Runnable` overloads when the mod only needs a stable tick signal, and use the native-argument overloads when the version-specific Minecraft object is useful. Detailed player interaction and networking still belong in the dedicated API surfaces as they grow.
