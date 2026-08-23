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

import foo.zaaarf.geb.api.IEvent;
import foo.zaaarf.geb.api.IEventDispatcher;
import foo.zaaarf.geb.api.IListener;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

final class NowsLifecycleDispatcher<T extends IEvent> implements IEventDispatcher<T> {
    private final Class<T> eventType;
    private final BiConsumer<NowsLifecycleListener, T> callback;

    NowsLifecycleDispatcher(Class<T> eventType, BiConsumer<NowsLifecycleListener, T> callback) {
        this.eventType = Objects.requireNonNull(eventType, "eventType");
        this.callback = Objects.requireNonNull(callback, "callback");
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
