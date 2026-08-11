package space.nows.mcnows.mc.api.event;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface GameEvents {
    void clientTick(Consumer<Minecraft> listener);

    void serverTick(Consumer<MinecraftServer> listener);

    void serverLevelTick(BiConsumer<MinecraftServer, ServerLevel> listener);
}
