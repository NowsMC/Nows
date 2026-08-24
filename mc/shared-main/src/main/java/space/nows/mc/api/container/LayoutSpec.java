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

import space.nows.mc.api.registry.ItemSpec;

import java.util.ArrayList;
import java.util.List;

/** Stable container layout for workstation and inventory-backed blocks. */
public final class LayoutSpec {
    private final String id;
    private final String blockId;
    private final int containerSlotCount;
    private final List<SlotSpec> slots;
    private final List<DataSlotSpec> data;

    private LayoutSpec(Builder builder) {
        this.id = ItemSpec.requireId(builder.id);
        this.blockId = builder.blockId == null ? null : ItemSpec.requireId(builder.blockId);
        this.containerSlotCount = builder.containerSlotCount;
        this.slots = List.copyOf(builder.slots);
        this.data = List.copyOf(builder.data);
        if (containerSlotCount < 0) {
            throw new IllegalArgumentException("containerSlotCount must be >= 0");
        }
        for (SlotSpec slot : slots) {
            if (slot.index() >= containerSlotCount) {
                throw new IllegalArgumentException("slot index must be < containerSlotCount");
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

    public int containerSlotCount() {
        return containerSlotCount;
    }

    public List<SlotSpec> slots() {
        return slots;
    }

    public List<DataSlotSpec> data() {
        return data;
    }

    public int dataCount() {
        return data.size();
    }

    public static final class Builder {
        private final String id;
        private String blockId;
        private int containerSlotCount;
        private final List<SlotSpec> slots = new ArrayList<>();
        private final List<DataSlotSpec> data = new ArrayList<>();

        private Builder(String id) {
            this.id = id;
        }

        public Builder block(String blockId) {
            this.blockId = blockId;
            return this;
        }

        public Builder containerSlotCount(int containerSlotCount) {
            this.containerSlotCount = containerSlotCount;
            return this;
        }

        public Builder slot(SlotSpec slot) {
            slots.add(slot);
            return this;
        }

        public Builder input(int index, int x, int y) {
            return slot(SlotSpec.input(index, x, y));
        }

        public Builder fuel(int index, int x, int y) {
            return slot(SlotSpec.fuel(index, x, y));
        }

        public Builder output(int index, int x, int y) {
            return slot(SlotSpec.output(index, x, y));
        }

        public Builder byproduct(int index, int x, int y) {
            return slot(SlotSpec.byproduct(index, x, y));
        }

        public Builder data(String name, int index) {
            data.add(new DataSlotSpec(name, index));
            return this;
        }

        public LayoutSpec build() {
            return new LayoutSpec(this);
        }
    }
}
