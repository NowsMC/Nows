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

package space.nows.mc.api;

/** Stable integer block position. */
public record McBlockPos(int x, int y, int z) {
    public static McBlockPos of(int x, int y, int z) {
        return new McBlockPos(x, y, z);
    }

    public McVec3 center() {
        return McVec3.of(x + 0.5D, y + 0.5D, z + 0.5D);
    }

    public McBlockPos offset(int dx, int dy, int dz) {
        return new McBlockPos(x + dx, y + dy, z + dz);
    }

    public McBlockPos relative(McDirection direction) {
        return offset(direction.stepX(), direction.stepY(), direction.stepZ());
    }
}
