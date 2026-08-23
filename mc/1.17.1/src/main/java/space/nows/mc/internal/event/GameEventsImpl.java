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

package space.nows.mc.internal.event;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import space.nows.mc.api.event.GameEvents;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class GameEventsImpl implements GameEvents {
    public static final GameEventsImpl INSTANCE = new GameEventsImpl();

    private final List<Consumer<Minecraft>> clientTickListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<MinecraftServer>> serverTickListeners = new CopyOnWriteArrayList<>();
    private final List<BiConsumer<MinecraftServer, ServerLevel>> serverLevelTickListeners = new CopyOnWriteArrayList<>();

    private GameEventsImpl() {
    }

    @Override
    public void clientTick(Consumer<Minecraft> listener) {
        clientTickListeners.add(listener);
    }

    @Override
    public void serverTick(Consumer<MinecraftServer> listener) {
        serverTickListeners.add(listener);
    }

    @Override
    public void serverLevelTick(BiConsumer<MinecraftServer, ServerLevel> listener) {
        serverLevelTickListeners.add(listener);
    }

    public void dispatchClientTick(Minecraft minecraft) {
        for (Consumer<Minecraft> listener : clientTickListeners) {
            listener.accept(minecraft);
        }
    }

    public void dispatchServerTick(MinecraftServer server) {
        for (Consumer<MinecraftServer> listener : serverTickListeners) {
            listener.accept(server);
        }
        for (ServerLevel level : server.getAllLevels()) {
            for (BiConsumer<MinecraftServer, ServerLevel> listener : serverLevelTickListeners) {
                listener.accept(server, level);
            }
        }
    }
}
