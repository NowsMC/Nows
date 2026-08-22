package space.nows.mcnows.mc.api.event;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface GameEvents {
    void clientTick(Consumer<Minecraft> listener);

    default void clientTick(Runnable listener) {
        clientTick(ignored -> listener.run());
    }


    void serverTick(Consumer<MinecraftServer> listener);

    default void serverTick(Runnable listener) {
        serverTick(ignored -> listener.run());
    }


    void serverLevelTick(BiConsumer<MinecraftServer, ServerLevel> listener);

    default void serverLevelTick(Runnable listener) {
        serverLevelTick((server, level) -> listener.run());
    }

}
