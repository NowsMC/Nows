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

package space.nows.mcnows.mc.api.nbt;

import java.util.ArrayList;
import java.util.List;

/** Stable list NBT data that can be translated to a version-specific Minecraft ListTag. */
public final class NbtList {
    private final List<NbtValue> values = new ArrayList<>();

    public NbtList add(NbtValue value) {
        values.add(value == null ? NbtValue.end() : value);
        return this;
    }

    public NbtList addString(String value) {
        return add(NbtValue.string(value));
    }

    public NbtList addInt(int value) {
        return add(NbtValue.integer(value));
    }

    public NbtList addCompound(NbtCompound value) {
        return add(NbtValue.compound(value));
    }

    public List<NbtValue> values() {
        return List.copyOf(values);
    }
}
