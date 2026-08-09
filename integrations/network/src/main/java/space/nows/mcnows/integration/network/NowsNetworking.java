package space.nows.mcnows.integration.network;

import io.netty.buffer.ByteBuf;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.api.NowsServices;
import space.nows.mcnows.api.NowsSide;
import space.nows.mcnows.core.mod.ModContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/** Mod-facing networking registry and transport facade. */
public final class NowsNetworking {
    private final NowsSide runtimeSide;
    private final Map<NetworkChannelId, NetworkChannel> channels = new LinkedHashMap<>();
    private final Map<NetworkDirection, Map<NetworkChannelId, List<NetworkPacketHandler>>> handlers =
            new EnumMap<>(NetworkDirection.class);
    private volatile NetworkTransport transport = UnavailableTransport.INSTANCE;

    public NowsNetworking(NowsSide runtimeSide) {
        this.runtimeSide = runtimeSide == null ? NowsSide.CLIENT : runtimeSide;
        for (NetworkDirection direction : NetworkDirection.values()) {
            handlers.put(direction, new LinkedHashMap<>());
        }
    }

    public static NowsNetworking install(NowsServices services, NowsSide runtimeSide) {
        NowsNetworking networking = new NowsNetworking(runtimeSide);
        services.register(NowsNetworking.class, networking);
        return networking;
    }

    public static NowsNetworking service(NowsContext context) {
        return context.service(NowsNetworking.class);
    }

    public NowsSide runtimeSide() {
        return runtimeSide;
    }

    public void installTransport(NetworkTransport transport) {
        this.transport = transport == null ? UnavailableTransport.INSTANCE : transport;
    }

    public NetworkChannel registerChannel(String id) {
        return registerChannel(NetworkChannelId.of(id), NetworkDirection.CLIENTBOUND, NetworkDirection.SERVERBOUND);
    }

    public NetworkChannel registerChannel(String id, NetworkDirection... directions) {
        return registerChannel(NetworkChannelId.of(id), directions);
    }

    public synchronized NetworkChannel registerChannel(NetworkChannelId id, NetworkDirection... directions) {
        NetworkChannel channel = new NetworkChannel(id, directions);
        NetworkChannel existing = channels.get(id);
        if (existing != null) {
            NetworkChannel merged = existing.merge(channel);
            channels.put(id, merged);
            return merged;
        }
        channels.put(id, channel);
        return channel;
    }

    public Optional<NetworkChannel> channel(String id) {
        return Optional.ofNullable(channels.get(NetworkChannelId.of(id)));
    }

    public List<NetworkChannel> channels() {
        return List.copyOf(channels.values());
    }

    public void registerHandler(String id, NetworkDirection direction, NetworkPacketHandler handler) {
        registerHandler(NetworkChannelId.of(id), direction, handler);
    }

    public synchronized void registerHandler(
            NetworkChannelId id,
            NetworkDirection direction,
            NetworkPacketHandler handler
    ) {
        if (handler == null) {
            throw new IllegalArgumentException("Network packet handler must not be null");
        }
        NetworkChannel channel = registerChannel(id, direction);
        if (!channel.supports(direction)) {
            throw new IllegalArgumentException("Network channel " + id + " does not support " + direction);
        }
        handlers.get(direction)
                .computeIfAbsent(id, ignored -> new CopyOnWriteArrayList<>())
                .add(handler);
    }

    public boolean canSend(String id, NetworkDirection direction) {
        return canSend(NetworkChannelId.of(id), direction);
    }

    public boolean canSend(NetworkChannelId id, NetworkDirection direction) {
        NetworkChannel channel = channels.get(id);
        return channel != null
                && channel.supports(direction)
                && direction.canSendFrom(runtimeSide)
                && transport.canSend(direction, id);
    }

    public boolean send(String id, NetworkDirection direction, byte[] payload) throws Exception {
        return send(NetworkChannelId.of(id), direction, NetworkPayload.of(payload));
    }

    public boolean send(String id, NetworkDirection direction, ByteBuf payload) throws Exception {
        return send(NetworkChannelId.of(id), direction, NetworkPayload.of(payload));
    }

    public boolean send(NetworkChannelId id, NetworkDirection direction, NetworkPayload payload) throws Exception {
        if (!canSend(id, direction)) {
            return false;
        }
        transport.send(direction, id, payload == null ? NetworkPayload.empty() : payload);
        return true;
    }

    public int receive(String id, NetworkDirection direction, byte[] payload) throws Exception {
        return receive(NetworkChannelId.of(id), direction, NetworkPayload.of(payload));
    }

    public int receive(String id, NetworkDirection direction, ByteBuf payload) throws Exception {
        return receive(NetworkChannelId.of(id), direction, NetworkPayload.wrap(payload));
    }

    public int receive(NetworkChannelId id, NetworkDirection direction, NetworkPayload payload) throws Exception {
        NetworkChannel channel = channels.get(id);
        if (channel == null || !channel.supports(direction)) {
            return 0;
        }
        if (!direction.canReceiveOn(runtimeSide)) {
            throw new IllegalStateException("Cannot receive " + direction + " packets on " + runtimeSide.metadataName());
        }
        List<NetworkPacketHandler> packetHandlers = handlers.get(direction).getOrDefault(id, List.of());
        NetworkPacketContext context = new NetworkPacketContext(runtimeSide, direction, id);
        for (NetworkPacketHandler handler : packetHandlers) {
            handler.handle(context, payload == null ? NetworkPayload.empty() : payload);
        }
        return packetHandlers.size();
    }

    public int registerDeclaredChannels(List<ModContainer> mods) {
        int count = 0;
        for (ModContainer mod : mods) {
            for (String id : mod.descriptor().declarations("network-channel")) {
                registerChannel(id);
                count++;
            }
            for (String id : mod.descriptor().declarations("network")) {
                registerChannel(id);
                count++;
            }
            for (String id : mod.descriptor().declarations("clientbound-channel")) {
                registerChannel(id, NetworkDirection.CLIENTBOUND);
                count++;
            }
            for (String id : mod.descriptor().declarations("serverbound-channel")) {
                registerChannel(id, NetworkDirection.SERVERBOUND);
                count++;
            }
        }
        return count;
    }

    public record NetworkChannel(NetworkChannelId id, List<NetworkDirection> directions) {
        public NetworkChannel(NetworkChannelId id, NetworkDirection... directions) {
            this(id, normalizeDirections(directions));
        }

        public NetworkChannel {
            if (id == null) {
                throw new IllegalArgumentException("Network channel id must not be null");
            }
            directions = List.copyOf(directions == null || directions.isEmpty()
                    ? List.of(NetworkDirection.CLIENTBOUND, NetworkDirection.SERVERBOUND)
                    : directions);
        }

        public boolean supports(NetworkDirection direction) {
            return directions.contains(direction);
        }

        private NetworkChannel merge(NetworkChannel other) {
            List<NetworkDirection> merged = new ArrayList<>(directions);
            for (NetworkDirection direction : other.directions) {
                if (!merged.contains(direction)) {
                    merged.add(direction);
                }
            }
            return new NetworkChannel(id, merged);
        }

        private static List<NetworkDirection> normalizeDirections(NetworkDirection[] directions) {
            if (directions == null || directions.length == 0) {
                return List.of(NetworkDirection.CLIENTBOUND, NetworkDirection.SERVERBOUND);
            }
            List<NetworkDirection> result = new ArrayList<>();
            Collections.addAll(result, directions);
            result.removeIf(direction -> direction == null);
            return result.isEmpty()
                    ? List.of(NetworkDirection.CLIENTBOUND, NetworkDirection.SERVERBOUND)
                    : List.copyOf(result);
        }
    }

    private enum UnavailableTransport implements NetworkTransport {
        INSTANCE;

        @Override
        public boolean canSend(NetworkDirection direction, NetworkChannelId channel) {
            return false;
        }

        @Override
        public void send(NetworkDirection direction, NetworkChannelId channel, NetworkPayload payload) {
            throw new IllegalStateException("No Nows network transport is installed");
        }
    }
}
