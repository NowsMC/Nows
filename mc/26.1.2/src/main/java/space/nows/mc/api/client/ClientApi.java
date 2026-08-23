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

package space.nows.mc.api.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Client helpers backed directly by the active Minecraft APIs. */
public final class ClientApi {
    private ClientApi() {}

    public static Minecraft minecraft() {
        return Minecraft.getInstance();
    }

    public static void show(Screen screen) {
        minecraft().setScreenAndShow(screen);
    }

    public static void execute(Runnable task) {
        minecraft().execute(task);
    }

    public static void close() {
        show(null);
    }
}
