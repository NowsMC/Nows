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

package space.nows.mc.api.machine;

import java.util.Objects;

/** Stable machine menu slot description with screen coordinates. */
public record MachineSlotSpec(
        int index,
        int x,
        int y,
        MachineSlotRole role,
        MachineSlotRule rule
) {
    public MachineSlotSpec {
        if (index < 0) {
            throw new IllegalArgumentException("index must be >= 0");
        }
        role = Objects.requireNonNull(role, "role");
        rule = Objects.requireNonNull(rule, "rule");
    }

    public static MachineSlotSpec input(int index, int x, int y) {
        return new MachineSlotSpec(index, x, y, MachineSlotRole.INPUT, MachineSlotRule.ANY);
    }

    public static MachineSlotSpec fuel(int index, int x, int y) {
        return new MachineSlotSpec(index, x, y, MachineSlotRole.FUEL, MachineSlotRule.FUEL);
    }

    public static MachineSlotSpec output(int index, int x, int y) {
        return new MachineSlotSpec(index, x, y, MachineSlotRole.OUTPUT, MachineSlotRule.NONE);
    }

    public static MachineSlotSpec byproduct(int index, int x, int y) {
        return new MachineSlotSpec(index, x, y, MachineSlotRole.BYPRODUCT, MachineSlotRule.NONE);
    }
}

