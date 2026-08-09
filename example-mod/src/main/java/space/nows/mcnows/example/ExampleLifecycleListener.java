package space.nows.mcnows.example;

import reactor.util.Logger;
import space.nows.mcnows.integration.geb.NowsLifecycleListener;
import space.nows.mcnows.integration.geb.event.NowsEntrypointsCompletedEvent;
import space.nows.mcnows.integration.logging.NowsLog;

public final class ExampleLifecycleListener implements NowsLifecycleListener {
    private static final Logger LOG = NowsLog.get(ExampleLifecycleListener.class);

    @Override
    public void onNowsEntrypointsCompleted(NowsEntrypointsCompletedEvent event) {
        LOG.info("Nows Example: GEB observed {} entrypoint(s)", event.count());
    }
}
