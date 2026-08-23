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

import foo.zaaarf.geb.api.IListener;
import space.nows.integration.geb.event.NowsBootstrapReadyEvent;
import space.nows.integration.geb.event.NowsEntrypointsCompletedEvent;
import space.nows.integration.geb.event.NowsEntrypointsStartingEvent;
import space.nows.integration.geb.event.NowsMinecraftStartingEvent;
import space.nows.integration.geb.event.NowsModEntrypointCompletedEvent;
import space.nows.integration.geb.event.NowsModEntrypointStartingEvent;
import space.nows.integration.geb.event.NowsRegisterEvent;

/** Listener interface for Nows-owned lifecycle events. */
public interface NowsLifecycleListener extends IListener {
    default void onNowsBootstrapReady(NowsBootstrapReadyEvent event) {
    }

    default void onNowsRegister(NowsRegisterEvent event) {
    }

    default void onNowsEntrypointsStarting(NowsEntrypointsStartingEvent event) {
    }

    default void onNowsModEntrypointStarting(NowsModEntrypointStartingEvent event) {
    }

    default void onNowsModEntrypointCompleted(NowsModEntrypointCompletedEvent event) {
    }

    default void onNowsEntrypointsCompleted(NowsEntrypointsCompletedEvent event) {
    }

    default void onNowsMinecraftStarting(NowsMinecraftStartingEvent event) {
    }
}
