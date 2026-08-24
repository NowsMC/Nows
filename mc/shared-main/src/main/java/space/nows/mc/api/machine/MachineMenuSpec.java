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

import space.nows.mc.api.registry.ItemSpec;

import java.util.ArrayList;
import java.util.List;

/** Stable container menu layout for machine blocks. */
public final class MachineMenuSpec {
    private final String id;
    private final String blockId;
    private final int machineSlotCount;
    private final List<MachineSlotSpec> slots;
    private final List<MachineDataSpec> data;

    private MachineMenuSpec(Builder builder) {
        this.id = ItemSpec.requireId(builder.id);
        this.blockId = builder.blockId == null ? null : ItemSpec.requireId(builder.blockId);
        this.machineSlotCount = builder.machineSlotCount;
        this.slots = List.copyOf(builder.slots);
        this.data = List.copyOf(builder.data);
        if (machineSlotCount < 0) {
            throw new IllegalArgumentException("machineSlotCount must be >= 0");
        }
        for (MachineSlotSpec slot : slots) {
            if (slot.index() >= machineSlotCount) {
                throw new IllegalArgumentException("machine slot index must be < machineSlotCount");
            }
        }
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public String id() {
        return id;
    }

    public String blockId() {
        return blockId;
    }

    public int machineSlotCount() {
        return machineSlotCount;
    }

    public List<MachineSlotSpec> slots() {
        return slots;
    }

    public List<MachineDataSpec> data() {
        return data;
    }

    public int dataCount() {
        return data.size();
    }

    public static final class Builder {
        private final String id;
        private String blockId;
        private int machineSlotCount;
        private final List<MachineSlotSpec> slots = new ArrayList<>();
        private final List<MachineDataSpec> data = new ArrayList<>();

        private Builder(String id) {
            this.id = id;
        }

        public Builder block(String blockId) {
            this.blockId = blockId;
            return this;
        }

        public Builder machineSlotCount(int machineSlotCount) {
            this.machineSlotCount = machineSlotCount;
            return this;
        }

        public Builder slot(MachineSlotSpec slot) {
            slots.add(slot);
            return this;
        }

        public Builder input(int index, int x, int y) {
            return slot(MachineSlotSpec.input(index, x, y));
        }

        public Builder fuel(int index, int x, int y) {
            return slot(MachineSlotSpec.fuel(index, x, y));
        }

        public Builder output(int index, int x, int y) {
            return slot(MachineSlotSpec.output(index, x, y));
        }

        public Builder byproduct(int index, int x, int y) {
            return slot(MachineSlotSpec.byproduct(index, x, y));
        }

        public Builder data(String name, int index) {
            data.add(new MachineDataSpec(name, index));
            return this;
        }

        public MachineMenuSpec build() {
            return new MachineMenuSpec(this);
        }
    }
}

