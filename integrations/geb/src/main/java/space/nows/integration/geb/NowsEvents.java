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
