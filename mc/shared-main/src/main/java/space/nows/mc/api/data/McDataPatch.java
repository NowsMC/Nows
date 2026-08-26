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

package space.nows.mc.api.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import space.nows.mc.api.registry.ItemSpec;

/** Stable set of data mutations for generated item/entity/block-entity code. */
public final class McDataPatch {
    private final List<McDataComponent> set;
    private final List<String> remove;

    private McDataPatch(Builder builder) {
        this.set = List.copyOf(builder.set);
        this.remove = List.copyOf(builder.remove);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<McDataComponent> set() {
        return set;
    }

    public List<String> remove() {
        return remove;
    }

    public static final class Builder {
        private final List<McDataComponent> set = new ArrayList<>();
        private final List<String> remove = new ArrayList<>();

        private Builder() {}

        public Builder set(McDataComponent component) {
            set.add(Objects.requireNonNull(component, "component"));
            return this;
        }

        public Builder remove(String componentId) {
            remove.add(ItemSpec.requireId(componentId));
            return this;
        }

        public McDataPatch build() {
            return new McDataPatch(this);
        }
    }
}
