# Lifecycle Events

`integrations/geb` registers a GEB instance in `NowsServices`. Mods that want it can use the small Nows facade:

```java
NowsEvents events = GebIntegration.events(context);
events.post(new MyCustomEvent());
```

Mods can declare no-argument listener classes in `nows.mod.kdl` with `listener "com.example.Listener"`. Nows registers those listeners before entrypoints run.

For Nows-owned loader lifecycle events, implement `NowsLifecycleListener`:

```java
public final class MyLifecycleListener implements NowsLifecycleListener {
    @Override
    public void onNowsEntrypointsCompleted(NowsEntrypointsCompletedEvent event) {
        int loadedEntrypoints = event.count();
    }
}
```

Lifecycle events include `NowsBootstrapReadyEvent`, `NowsEntrypointsStartingEvent`, `NowsModEntrypointStartingEvent`, `NowsModEntrypointCompletedEvent`, `NowsEntrypointsCompletedEvent` and `NowsMinecraftStartingEvent`.

A mod that does not care about GEB can still compile against `nows-core` without pulling GEB into its API.

## When entrypoints run

Mod entrypoints can read and register registry content such as items, blocks and sounds. Minecraft 1.17 and newer only allow that after `Bootstrap` has run and before the built-in registries are frozen, so the adapters for those versions run mod entrypoints in that window instead of before Minecraft's `main` starts. `NowsMinecraftStartingEvent` still comes after the entrypoints. The 1.16.x adapters have no such restriction and run entrypoints before `main`.

Minecraft 26.x builds an item's data components only after a world is joined, so a native `ItemStack` cannot be created during initialization there. The `McItemStack` overloads of the recipe viewer API resolve their stacks when they are read for this reason, so register catalysts and category icons with them.
