package space.nows.mcnows.integration.geb;

import foo.zaaarf.geb.GEB;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.api.NowsServices;

public final class GebIntegration {
    private GebIntegration() {}

    public static GEB install(NowsServices services, ClassLoader gameLoader) {
        GEB bus = new GEB();
        bus.loadAndRegisterDispatchers(gameLoader);
        services.register(GEB.class, bus);
        return bus;
    }

    public static GEB eventBus(NowsContext context) {
        return context.service(GEB.class);
    }
}
