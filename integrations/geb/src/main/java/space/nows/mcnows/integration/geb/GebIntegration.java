package space.nows.mcnows.integration.geb;

import foo.zaaarf.geb.GEB;
import foo.zaaarf.geb.api.IListener;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.api.NowsServices;
import space.nows.mcnows.core.mod.ModContainer;
import space.nows.mcnows.integration.geb.event.NowsBootstrapReadyEvent;
import space.nows.mcnows.integration.geb.event.NowsEntrypointsCompletedEvent;
import space.nows.mcnows.integration.geb.event.NowsEntrypointsStartingEvent;
import space.nows.mcnows.integration.geb.event.NowsMinecraftStartingEvent;
import space.nows.mcnows.integration.geb.event.NowsModEntrypointCompletedEvent;
import space.nows.mcnows.integration.geb.event.NowsModEntrypointStartingEvent;

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
