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

/** Stable synced integer data slot used by a container layout. */
public record DataSlotSpec(String name, int index) {
    public DataSlotSpec {
        name = ItemSpec.requireId(name);
        if (index < 0) {
            throw new IllegalArgumentException("index must be >= 0");
        }
    }
}
