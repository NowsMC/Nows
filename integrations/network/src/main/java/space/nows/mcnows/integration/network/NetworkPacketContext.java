package space.nows.mcnows.integration.network;

import space.nows.mcnows.api.NowsSide;

/** Context supplied to packet handlers. */
public record NetworkPacketContext(
        NowsSide runtimeSide,
        NetworkDirection direction,
        NetworkChannelId channel
) {
}
