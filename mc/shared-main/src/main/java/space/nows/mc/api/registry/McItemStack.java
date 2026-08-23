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

import space.nows.mc.api.nbt.NbtCompound;

import java.util.Objects;

/** Stable item stack description with optional Nows-owned data payload. */
public record McItemStack(String itemId, int count, NbtCompound data) {
    public McItemStack {
        ItemSpec.requireId(itemId);
        if (count < 1) {
            throw new IllegalArgumentException("count must be >= 1");
        }
    }

    public static McItemStack of(String itemId) {
        return of(itemId, 1);
    }

    public static McItemStack of(String itemId, int count) {
        return new McItemStack(itemId, count, null);
    }

    public static McItemStack of(String itemId, int count, NbtCompound data) {
        return new McItemStack(itemId, count, data);
    }

    public static McItemStack of(ItemStackSpec spec) {
        Objects.requireNonNull(spec, "spec");
        return of(spec.itemId(), spec.count());
    }

    public ItemStackSpec toSpec() {
        return ItemStackSpec.of(itemId, count);
    }
}
