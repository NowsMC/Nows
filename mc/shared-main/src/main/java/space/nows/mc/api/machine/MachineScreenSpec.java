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

package space.nows.mc.api.machine;

import space.nows.mc.api.registry.ItemSpec;
import space.nows.mc.api.text.McText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Stable texture-backed screen description for a machine menu. */
public final class MachineScreenSpec {
    private final String menuId;
    private final McText title;
    private final String backgroundTextureId;
    private final int width;
    private final int height;
    private final int inventoryLabelY;
    private final List<MachineProgressSpec> progressBars;

    private MachineScreenSpec(Builder builder) {
        this.menuId = ItemSpec.requireId(builder.menuId);
        this.title = Objects.requireNonNull(builder.title, "title");
        this.backgroundTextureId = ItemSpec.requireId(builder.backgroundTextureId);
        this.width = builder.width;
        this.height = builder.height;
        this.inventoryLabelY = builder.inventoryLabelY;
        this.progressBars = List.copyOf(builder.progressBars);
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("width and height must be >= 1");
        }
    }

    public static Builder builder(String menuId, McText title, String backgroundTextureId) {
        return new Builder(menuId, title, backgroundTextureId);
    }

    public String menuId() {
        return menuId;
    }

    public McText title() {
        return title;
    }

    public String backgroundTextureId() {
        return backgroundTextureId;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int inventoryLabelY() {
        return inventoryLabelY;
    }

    public List<MachineProgressSpec> progressBars() {
        return progressBars;
    }

    public static final class Builder {
        private final String menuId;
        private final McText title;
        private final String backgroundTextureId;
        private int width = 176;
        private int height = 166;
        private int inventoryLabelY = 72;
        private final List<MachineProgressSpec> progressBars = new ArrayList<>();

        private Builder(String menuId, McText title, String backgroundTextureId) {
            this.menuId = menuId;
            this.title = title;
            this.backgroundTextureId = backgroundTextureId;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder inventoryLabelY(int inventoryLabelY) {
            this.inventoryLabelY = inventoryLabelY;
            return this;
        }

        public Builder progress(MachineProgressSpec progress) {
            progressBars.add(progress);
            return this;
        }

        public MachineScreenSpec build() {
            return new MachineScreenSpec(this);
        }
    }
}

