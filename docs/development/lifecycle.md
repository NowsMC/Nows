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
