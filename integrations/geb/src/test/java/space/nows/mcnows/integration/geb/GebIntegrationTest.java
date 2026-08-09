package space.nows.mcnows.integration.geb;

import org.junit.jupiter.api.Test;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.api.NowsServices;
import space.nows.mcnows.integration.geb.event.NowsEntrypointsCompletedEvent;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GebIntegrationTest {
    @Test
    void postsNowsLifecycleEventsToDeclaredLifecycleListeners() {
        NowsServices services = new NowsServices();
        GebIntegration.install(services, getClass().getClassLoader());
        NowsContext context = new NowsContext(
                "26.2",
                Path.of(".minecraft"),
                List.of(),
                getClass().getClassLoader(),
                services);
        CountingLifecycleListener listener = new CountingLifecycleListener();

        NowsEvents events = GebIntegration.events(context);
        events.register(listener);
        events.post(new NowsEntrypointsCompletedEvent(context, 3));

        assertEquals(3, listener.entrypoints);
    }

    private static final class CountingLifecycleListener implements NowsLifecycleListener {
        private int entrypoints;

        @Override
        public void onNowsEntrypointsCompleted(NowsEntrypointsCompletedEvent event) {
            entrypoints = event.count();
        }
    }
}
