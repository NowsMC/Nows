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

import space.nows.mc.api.nbt.NbtValue;
import space.nows.mc.api.registry.ItemSpec;

/** Stable named data value that adapters can map to NBT or modern data components. */
public record McDataComponent(String id, NbtValue value) {
    public McDataComponent {
        ItemSpec.requireId(id);
    }

    public static McDataComponent of(String id, NbtValue value) {
        return new McDataComponent(id, value);
    }
}
