package space.nows.mcnows.integration.network;

/** Version-specific Minecraft code implements this to move packets over the real connection. */
public interface NetworkTransport {
    boolean canSend(NetworkDirection direction, NetworkChannelId channel);

    void send(NetworkDirection direction, NetworkChannelId channel, NetworkPayload payload) throws Exception;
}
