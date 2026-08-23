/*
 * Copyright 2026 TamKungZ_ (Nows MC — https://nows.space)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.nows.integration.geb;

import org.junit.jupiter.api.Test;
import space.nows.platform.api.NowsContext;
import space.nows.platform.api.NowsSide;
import space.nows.platform.api.NowsServices;
import space.nows.integration.geb.event.NowsEntrypointsCompletedEvent;

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
                NowsSide.CLIENT,
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
