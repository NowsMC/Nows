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

import space.nows.platform.api.NowsSide;

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
