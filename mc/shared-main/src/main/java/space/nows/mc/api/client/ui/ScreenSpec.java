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
import space.nows.mc.api.text.McText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Stable texture-backed screen description for a container UI. */
public final class ScreenSpec {
    private final String layoutId;
    private final McText title;
    private final String backgroundTextureId;
    private final int width;
    private final int height;
    private final int inventoryLabelY;
    private final List<ScreenProgressSpec> progressBars;

    private ScreenSpec(Builder builder) {
        this.layoutId = ItemSpec.requireId(builder.layoutId);
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

    public static Builder builder(String layoutId, McText title, String backgroundTextureId) {
        return new Builder(layoutId, title, backgroundTextureId);
    }

    public String layoutId() {
        return layoutId;
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

    public List<ScreenProgressSpec> progressBars() {
        return progressBars;
    }

    public static final class Builder {
        private final String layoutId;
        private final McText title;
        private final String backgroundTextureId;
        private int width = 176;
        private int height = 166;
        private int inventoryLabelY = 72;
        private final List<ScreenProgressSpec> progressBars = new ArrayList<>();

        private Builder(String layoutId, McText title, String backgroundTextureId) {
            this.layoutId = layoutId;
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

        public Builder progress(ScreenProgressSpec progress) {
            progressBars.add(progress);
            return this;
        }

        public ScreenSpec build() {
            return new ScreenSpec(this);
        }
    }
}
