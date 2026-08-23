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

package space.nows.integration.network;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import space.nows.platform.api.NowsSide;

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
        int handlers = networking.receive("example:main", NetworkDirection.CLIENTBOUND,
                Unpooled.wrappedBuffer(new byte[] {1, 2, 3}));

        assertEquals(1, handlers);
        assertEquals(3, bytes.get());
    }

    @Test
    void sendsThroughInstalledTransportWhenDirectionMatchesRuntimeSide() throws Exception {
        NowsNetworking networking = new NowsNetworking(NowsSide.CLIENT);
        RecordingTransport transport = new RecordingTransport();
        networking.installTransport(transport);
        networking.registerChannel("example:main", NetworkDirection.SERVERBOUND);

        assertTrue(networking.send("example:main", NetworkDirection.SERVERBOUND,
                Unpooled.wrappedBuffer(new byte[] {7})));

        assertEquals(NetworkChannelId.of("example:main"), transport.channel);
        assertEquals(1, transport.payload.size());
        assertEquals(7, transport.payload.buffer().readByte());
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
