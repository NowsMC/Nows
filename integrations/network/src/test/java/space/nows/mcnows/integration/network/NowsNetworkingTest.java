package space.nows.mcnows.integration.network;

import org.junit.jupiter.api.Test;
import space.nows.mcnows.api.NowsSide;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NowsNetworkingTest {
    @Test
    void dispatchesReceivedPacketsToRegisteredHandlers() throws Exception {
        NowsNetworking networking = new NowsNetworking(NowsSide.CLIENT);
        AtomicInteger bytes = new AtomicInteger();

        networking.registerHandler("example:main", NetworkDirection.CLIENTBOUND,
                (context, payload) -> bytes.set(payload.size()));
        int handlers = networking.receive("example:main", NetworkDirection.CLIENTBOUND, new byte[] {1, 2, 3});

        assertEquals(1, handlers);
        assertEquals(3, bytes.get());
    }

    @Test
    void sendsThroughInstalledTransportWhenDirectionMatchesRuntimeSide() throws Exception {
        NowsNetworking networking = new NowsNetworking(NowsSide.CLIENT);
        RecordingTransport transport = new RecordingTransport();
        networking.installTransport(transport);
        networking.registerChannel("example:main", NetworkDirection.SERVERBOUND);

        assertTrue(networking.send("example:main", NetworkDirection.SERVERBOUND, new byte[] {7}));

        assertEquals(NetworkChannelId.of("example:main"), transport.channel);
        assertEquals(1, transport.payload.size());
    }

    @Test
    void refusesSendWhenNoTransportIsInstalled() throws Exception {
        NowsNetworking networking = new NowsNetworking(NowsSide.CLIENT);
        networking.registerChannel("example:main", NetworkDirection.SERVERBOUND);

        assertFalse(networking.send("example:main", NetworkDirection.SERVERBOUND, new byte[] {7}));
    }

    private static final class RecordingTransport implements NetworkTransport {
        private NetworkChannelId channel;
        private NetworkPayload payload = NetworkPayload.empty();

        @Override
        public boolean canSend(NetworkDirection direction, NetworkChannelId channel) {
            return true;
        }

        @Override
        public void send(NetworkDirection direction, NetworkChannelId channel, NetworkPayload payload) {
            this.channel = channel;
            this.payload = payload;
        }
    }
}
