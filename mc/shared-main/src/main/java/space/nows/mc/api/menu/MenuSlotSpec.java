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

package space.nows.mc.api.menu;

import java.util.Objects;

/** Stable container menu slot description with screen coordinates. */
public record MenuSlotSpec(
        int index,
        int x,
        int y,
        MenuSlotRole role,
        MenuSlotRule rule
) {
    public MenuSlotSpec {
        if (index < 0) {
            throw new IllegalArgumentException("index must be >= 0");
        }
        role = Objects.requireNonNull(role, "role");
        rule = Objects.requireNonNull(rule, "rule");
    }

    public static MenuSlotSpec input(int index, int x, int y) {
        return new MenuSlotSpec(index, x, y, MenuSlotRole.INPUT, MenuSlotRule.ANY);
    }

    public static MenuSlotSpec fuel(int index, int x, int y) {
        return new MenuSlotSpec(index, x, y, MenuSlotRole.FUEL, MenuSlotRule.FUEL);
    }

    public static MenuSlotSpec output(int index, int x, int y) {
        return new MenuSlotSpec(index, x, y, MenuSlotRole.OUTPUT, MenuSlotRule.NONE);
    }

    public static MenuSlotSpec byproduct(int index, int x, int y) {
        return new MenuSlotSpec(index, x, y, MenuSlotRole.BYPRODUCT, MenuSlotRule.NONE);
    }
}
