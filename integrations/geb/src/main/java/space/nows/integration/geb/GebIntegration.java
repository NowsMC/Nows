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

import foo.zaaarf.geb.GEB;
import foo.zaaarf.geb.api.IListener;
import space.nows.platform.api.NowsContext;
import space.nows.platform.api.NowsServices;
import space.nows.platform.core.mod.ModContainer;
import space.nows.integration.geb.event.NowsBootstrapReadyEvent;
import space.nows.integration.geb.event.NowsEntrypointsCompletedEvent;
import space.nows.integration.geb.event.NowsEntrypointsStartingEvent;
import space.nows.integration.geb.event.NowsMinecraftStartingEvent;
import space.nows.integration.geb.event.NowsModEntrypointCompletedEvent;
import space.nows.integration.geb.event.NowsModEntrypointStartingEvent;
import space.nows.integration.geb.event.NowsRegisterEvent;

import java.util.List;

public final class GebIntegration {
    private GebIntegration() {}

    public static GEB install(NowsServices services, ClassLoader gameLoader) {
        GEB bus = new GEB();
        bus.loadAndRegisterDispatchers(gameLoader);
        registerNowsLifecycleDispatchers(bus);
        services.register(GEB.class, bus);
        services.register(NowsEvents.class, new NowsEvents(bus));
        return bus;
    }

    public static GEB eventBus(NowsContext context) {
        return context.service(GEB.class);
    }

    public static NowsEvents events(NowsContext context) {
        return context.service(NowsEvents.class);
    }

    public static int registerDeclaredListeners(NowsContext context) throws Exception {
        return registerDeclaredListeners(events(context), context.gameClassLoader(), context.mods());
    }

    public static int registerDeclaredListeners(NowsEvents events, ClassLoader loader, List<ModContainer> mods) throws Exception {
        int count = 0;
        for (ModContainer mod : mods) {
            for (String className : mod.descriptor().declarations("listener")) {
                registerListener(events, loader, className);
                count++;
            }
            for (String className : mod.descriptor().declarations("geb-listener")) {
                registerListener(events, loader, className);
                count++;
            }
        }
        return count;
    }

    private static void registerListener(NowsEvents events, ClassLoader loader, String className) throws Exception {
        Object instance = Class.forName(className, true, loader).getDeclaredConstructor().newInstance();
        if (!(instance instanceof IListener listener)) {
            throw new IllegalStateException(className + " does not implement " + IListener.class.getName());
        }
        events.register(listener);
    }

    private static void registerNowsLifecycleDispatchers(GEB bus) {
        bus.registerDispatcher(new NowsLifecycleDispatcher<>(
                NowsBootstrapReadyEvent.class, NowsLifecycleListener::onNowsBootstrapReady));
        bus.registerDispatcher(new NowsLifecycleDispatcher<>(
                NowsRegisterEvent.class, NowsLifecycleListener::onNowsRegister));
        bus.registerDispatcher(new NowsLifecycleDispatcher<>(
                NowsEntrypointsStartingEvent.class, NowsLifecycleListener::onNowsEntrypointsStarting));
        bus.registerDispatcher(new NowsLifecycleDispatcher<>(
                NowsModEntrypointStartingEvent.class, NowsLifecycleListener::onNowsModEntrypointStarting));
        bus.registerDispatcher(new NowsLifecycleDispatcher<>(
                NowsModEntrypointCompletedEvent.class, NowsLifecycleListener::onNowsModEntrypointCompleted));
        bus.registerDispatcher(new NowsLifecycleDispatcher<>(
                NowsEntrypointsCompletedEvent.class, NowsLifecycleListener::onNowsEntrypointsCompleted));
        bus.registerDispatcher(new NowsLifecycleDispatcher<>(
                NowsMinecraftStartingEvent.class, NowsLifecycleListener::onNowsMinecraftStarting));
    }
}
