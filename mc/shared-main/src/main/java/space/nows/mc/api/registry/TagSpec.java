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

import java.util.List;

/** Stable data-generation tag description. */
public record TagSpec(TagTarget target, String id, List<String> values) {
    public TagSpec {
        if (target == null) {
            target = TagTarget.ITEMS;
        }
        ItemSpec.requireId(id);
        values = values == null ? List.of() : List.copyOf(values);
    }

    public static TagSpec items(String id, List<String> values) {
        return new TagSpec(TagTarget.ITEMS, id, values);
    }

    public static TagSpec blocks(String id, List<String> values) {
        return new TagSpec(TagTarget.BLOCKS, id, values);
    }

    public enum TagTarget {
        ITEMS,
        BLOCKS,
        FLUIDS,
        ENTITY_TYPES
    }
}
