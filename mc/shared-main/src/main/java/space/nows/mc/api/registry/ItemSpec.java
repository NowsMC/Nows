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

package space.nows.mc.api.registry;

import java.util.Objects;

/** Stable Nows item registration data, translated by each Minecraft adapter. */
public final class ItemSpec {
    private final String id;
    private final int maxStackSize;
    private final boolean fireResistant;

    private ItemSpec(Builder builder) {
        this.id = requireId(builder.id);
        this.maxStackSize = builder.maxStackSize;
        this.fireResistant = builder.fireResistant;
        if (maxStackSize < 1 || maxStackSize > 99) {
            throw new IllegalArgumentException("maxStackSize must be between 1 and 99");
        }
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public String id() {
        return id;
    }

    public int maxStackSize() {
        return maxStackSize;
    }

    public boolean fireResistant() {
        return fireResistant;
    }

    static String requireId(String id) {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return id;
    }

    public static final class Builder {
        private final String id;
        private int maxStackSize = 64;
        private boolean fireResistant;

        private Builder(String id) {
            this.id = id;
        }

        public Builder maxStackSize(int maxStackSize) {
            this.maxStackSize = maxStackSize;
            return this;
        }

        public Builder fireResistant() {
            this.fireResistant = true;
            return this;
        }

        public ItemSpec build() {
            return new ItemSpec(this);
        }
    }
}
