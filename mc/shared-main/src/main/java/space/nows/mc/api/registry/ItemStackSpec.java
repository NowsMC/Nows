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

/** Stable item stack request translated to Minecraft ItemStack by each registry/player adapter. */
public record ItemStackSpec(String itemId, int count) {
    public ItemStackSpec {
        ItemSpec.requireId(itemId);
        if (count < 1 || count > 99) {
            throw new IllegalArgumentException("count must be between 1 and 99");
        }
    }

    public static ItemStackSpec of(String itemId) {
        return new ItemStackSpec(itemId, 1);
    }

    public static ItemStackSpec of(String itemId, int count) {
        return new ItemStackSpec(itemId, count);
    }
}
