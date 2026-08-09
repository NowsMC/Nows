package space.nows.mcnows.integration.network;

@FunctionalInterface
public interface NetworkPacketHandler {
    void handle(NetworkPacketContext context, NetworkPayload payload) throws Exception;
}
