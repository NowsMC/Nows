package space.nows.mcnows.integration.geb;

import foo.zaaarf.geb.api.IEvent;
import foo.zaaarf.geb.api.IEventDispatcher;
import foo.zaaarf.geb.api.IListener;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

final class NowsLifecycleDispatcher<T extends IEvent> implements IEventDispatcher<T> {
    private final Class<T> eventType;
    private final BiConsumer<NowsLifecycleListener, T> callback;

    NowsLifecycleDispatcher(Class<T> eventType, BiConsumer<NowsLifecycleListener, T> callback) {
        this.eventType = eventType;
        this.callback = callback;
    }

    @Override
    public boolean callListeners(T event, Map<Class<? extends IListener>, Set<IListener>> listeners) {
        for (Set<IListener> listenerSet : listeners.values()) {
            for (IListener listener : listenerSet) {
                if (listener instanceof NowsLifecycleListener lifecycleListener) {
                    callback.accept(lifecycleListener, event);
                }
            }
        }
        return true;
    }

    @Override
    public Class<T> eventType() {
        return eventType;
    }
}
