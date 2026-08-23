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

package space.nows.mc.api.recipe;

import space.nows.mc.api.registry.ItemSpec;

import java.util.List;

/** Stable recipe ingredient described by item ids or a tag id. */
public record IngredientSpec(List<String> itemIds, String tagId) {
    public IngredientSpec {
        itemIds = itemIds == null ? List.of() : List.copyOf(itemIds);
        itemIds.forEach(ItemSpec::requireId);
        if (tagId != null) {
            ItemSpec.requireId(tagId);
        }
        if (itemIds.isEmpty() && tagId == null) {
            throw new IllegalArgumentException("ingredient must have itemIds or tagId");
        }
    }

    public static IngredientSpec item(String itemId) {
        return new IngredientSpec(List.of(itemId), null);
    }

    public static IngredientSpec items(List<String> itemIds) {
        return new IngredientSpec(itemIds, null);
    }

    public static IngredientSpec tag(String tagId) {
        return new IngredientSpec(List.of(), tagId);
    }
}
