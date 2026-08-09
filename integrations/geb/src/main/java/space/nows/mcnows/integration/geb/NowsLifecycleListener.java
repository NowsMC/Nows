package space.nows.mcnows.integration.geb;

import foo.zaaarf.geb.api.IListener;
import space.nows.mcnows.integration.geb.event.NowsBootstrapReadyEvent;
import space.nows.mcnows.integration.geb.event.NowsEntrypointsCompletedEvent;
import space.nows.mcnows.integration.geb.event.NowsEntrypointsStartingEvent;
import space.nows.mcnows.integration.geb.event.NowsMinecraftStartingEvent;
import space.nows.mcnows.integration.geb.event.NowsModEntrypointCompletedEvent;
import space.nows.mcnows.integration.geb.event.NowsModEntrypointStartingEvent;

/** Listener interface for Nows-owned lifecycle events. */
public interface NowsLifecycleListener extends IListener {
    default void onNowsBootstrapReady(NowsBootstrapReadyEvent event) {
    }

    default void onNowsEntrypointsStarting(NowsEntrypointsStartingEvent event) {
    }

    default void onNowsModEntrypointStarting(NowsModEntrypointStartingEvent event) {
    }

    default void onNowsModEntrypointCompleted(NowsModEntrypointCompletedEvent event) {
    }

    default void onNowsEntrypointsCompleted(NowsEntrypointsCompletedEvent event) {
    }

    default void onNowsMinecraftStarting(NowsMinecraftStartingEvent event) {
    }
}
