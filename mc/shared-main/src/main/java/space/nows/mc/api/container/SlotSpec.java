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

package space.nows.mc.api.container;

import java.util.Objects;

/** Stable container slot description with screen coordinates. */
public record SlotSpec(
        int index,
        int x,
        int y,
        SlotRole role,
        SlotRule rule
) {
    public SlotSpec {
        if (index < 0) {
            throw new IllegalArgumentException("index must be >= 0");
        }
        role = Objects.requireNonNull(role, "role");
        rule = Objects.requireNonNull(rule, "rule");
    }

    public static SlotSpec input(int index, int x, int y) {
        return new SlotSpec(index, x, y, SlotRole.INPUT, SlotRule.ANY);
    }

    public static SlotSpec fuel(int index, int x, int y) {
        return new SlotSpec(index, x, y, SlotRole.FUEL, SlotRule.FUEL);
    }

    public static SlotSpec output(int index, int x, int y) {
        return new SlotSpec(index, x, y, SlotRole.OUTPUT, SlotRule.NONE);
    }

    public static SlotSpec byproduct(int index, int x, int y) {
        return new SlotSpec(index, x, y, SlotRole.BYPRODUCT, SlotRule.NONE);
    }
}
