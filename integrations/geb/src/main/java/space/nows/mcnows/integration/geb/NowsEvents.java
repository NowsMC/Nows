package space.nows.mcnows.integration.geb;

import foo.zaaarf.geb.GEB;
import foo.zaaarf.geb.api.IBus;
import foo.zaaarf.geb.api.IEvent;
import foo.zaaarf.geb.api.IListener;

/** Small Nows-facing facade over GEB's event bus. */
public final class NowsEvents {
    private final GEB bus;

    public NowsEvents(GEB bus) {
        this.bus = bus;
    }

    public GEB bus() {
        return bus;
    }

    public boolean post(IEvent event) {
        return bus.handleEvent(event);
    }

    public void register(IListener listener) {
        bus.registerListener(listener);
    }

    public void unregister(IListener listener) {
        bus.unregisterListener(listener);
    }

    public boolean isRegistered(IListener listener) {
        return bus.isRegistered(listener);
    }

    public void registerSubBus(IBus subBus, int priority) {
        bus.registerSubBus(subBus, priority);
    }

    public void unregisterSubBus(IBus subBus) {
        bus.unregisterSubBus(subBus);
    }
}
