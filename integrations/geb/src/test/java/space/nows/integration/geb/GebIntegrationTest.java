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
import space.nows.platform.core.mod.ModContainer;
import space.nows.platform.core.mod.ModDescriptor;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void rejectsInvalidFacadeInputs() {
        NowsServices services = new NowsServices();
        GebIntegration.install(services, getClass().getClassLoader());
        NowsEvents events = services.require(NowsEvents.class);

        assertThrows(NullPointerException.class, () -> new NowsEvents(null));
        assertThrows(NullPointerException.class, () -> events.post(null));
        assertThrows(NullPointerException.class, () -> events.register(null));
        assertThrows(NullPointerException.class, () -> events.unregister(null));
        assertThrows(NullPointerException.class, () -> events.isRegistered(null));
    }

    @Test
    void reportsModIdForDeclaredNonListenerClasses() {
        NowsServices services = new NowsServices();
        GebIntegration.install(services, getClass().getClassLoader());
        ModContainer mod = new ModContainer(
                Path.of("mods/bad-listener.jar"),
                new ModDescriptor(
                        "bad_listener",
                        "Bad Listener",
                        "1.0.0",
                        "26.2",
                        Map.of("listener", List.of(String.class.getName()))));

        assertThrows(IllegalStateException.class, () -> GebIntegration.registerDeclaredListeners(
                services.require(NowsEvents.class),
                getClass().getClassLoader(),
                List.of(mod)));
    }

    private static final class CountingLifecycleListener implements NowsLifecycleListener {
        private int entrypoints;

        @Override
        public void onNowsEntrypointsCompleted(NowsEntrypointsCompletedEvent event) {
            entrypoints = event.count();
        }
    }
}
