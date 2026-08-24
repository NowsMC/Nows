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

package space.nows.mc.api.client.ui;

import space.nows.mc.api.registry.ItemSpec;

import java.util.Objects;

/** Stable screen progress bar backed by two synced data slots. */
public record ScreenProgressSpec(
        String name,
        int x,
        int y,
        int width,
        int height,
        int valueDataIndex,
        int maxDataIndex,
        ProgressDirection direction
) {
    public ScreenProgressSpec {
        name = ItemSpec.requireId(name);
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("width and height must be >= 1");
        }
        if (valueDataIndex < 0 || maxDataIndex < 0) {
            throw new IllegalArgumentException("data indexes must be >= 0");
        }
        direction = Objects.requireNonNull(direction, "direction");
    }

    public static ScreenProgressSpec horizontal(
            String name,
            int x,
            int y,
            int width,
            int height,
            int valueDataIndex,
            int maxDataIndex
    ) {
        return new ScreenProgressSpec(
                name,
                x,
                y,
                width,
                height,
                valueDataIndex,
                maxDataIndex,
                ProgressDirection.LEFT_TO_RIGHT);
    }
}

