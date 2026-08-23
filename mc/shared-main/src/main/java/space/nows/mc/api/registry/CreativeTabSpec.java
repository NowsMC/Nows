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

import space.nows.mc.api.text.McText;

import java.util.ArrayList;
import java.util.List;

/** Stable creative-tab declaration for generated content code. */
public final class CreativeTabSpec {
    private final String id;
    private final McText title;
    private final McItemStack icon;
    private final List<McItemStack> entries;

    private CreativeTabSpec(Builder builder) {
        this.id = ItemSpec.requireId(builder.id);
        this.title = builder.title == null ? McText.literal(id) : builder.title;
        this.icon = builder.icon == null ? McItemStack.of("minecraft:stone") : builder.icon;
        this.entries = List.copyOf(builder.entries);
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public String id() {
        return id;
    }

    public McText title() {
        return title;
    }

    public McItemStack icon() {
        return icon;
    }

    public List<McItemStack> entries() {
        return entries;
    }

    public static final class Builder {
        private final String id;
        private McText title;
        private McItemStack icon;
        private final List<McItemStack> entries = new ArrayList<>();

        private Builder(String id) {
            this.id = id;
        }

        public Builder title(McText title) {
            this.title = title;
            return this;
        }

        public Builder icon(McItemStack icon) {
            this.icon = icon;
            return this;
        }

        public Builder entry(McItemStack entry) {
            entries.add(entry);
            return this;
        }

        public Builder entry(String itemId) {
            return entry(McItemStack.of(itemId));
        }

        public CreativeTabSpec build() {
            return new CreativeTabSpec(this);
        }
    }
}
