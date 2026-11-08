package space.nows.fixture.consumer;

import space.nows.integration.geb.event.NowsBootstrapReadyEvent;
import space.nows.integration.geb.NowsLifecycleListener;
import space.nows.integration.geb.event.NowsMinecraftStartingEvent;
import space.nows.integration.geb.event.NowsRegisterEvent;

/** Declared in nows.mod.kdl, so the loader instantiates it and registers it before entrypoints run. */
public final class FixtureListener implements NowsLifecycleListener {
    static volatile boolean bootstrapReady;
    static volatile boolean register;
    static volatile boolean minecraftStarting;
    static volatile boolean entrypointFinished;
    static volatile boolean startedAfterEntrypoint;

    @Override
    public void onNowsBootstrapReady(NowsBootstrapReadyEvent event) {
        bootstrapReady = true;
    }

    @Override
    public void onNowsRegister(NowsRegisterEvent event) {
        register = true;
    }

    @Override
    public void onNowsMinecraftStarting(NowsMinecraftStartingEvent event) {
        minecraftStarting = true;
        startedAfterEntrypoint = entrypointFinished;
    }
}
