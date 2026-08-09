package space.nows.mcnows.integration.network;

import space.nows.mcnows.api.NowsSide;

/** Packet direction from the physical side that sends the packet. */
public enum NetworkDirection {
    CLIENTBOUND,
    SERVERBOUND;

    public NowsSide receiverSide() {
        return this == CLIENTBOUND ? NowsSide.CLIENT : NowsSide.SERVER;
    }

    public NowsSide senderSide() {
        return this == CLIENTBOUND ? NowsSide.SERVER : NowsSide.CLIENT;
    }

    public boolean canReceiveOn(NowsSide side) {
        return receiverSide() == side;
    }

    public boolean canSendFrom(NowsSide side) {
        return senderSide() == side;
    }
}
