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

/** Stable food item facts translated by each Minecraft adapter. */
public final class FoodSpec {
    private final int nutrition;
    private final float saturationModifier;
    private final boolean alwaysEdible;
    private final boolean fast;
    private final boolean meat;

    private FoodSpec(Builder builder) {
        this.nutrition = builder.nutrition;
        this.saturationModifier = builder.saturationModifier;
        this.alwaysEdible = builder.alwaysEdible;
        this.fast = builder.fast;
        this.meat = builder.meat;
        if (nutrition < 0) {
            throw new IllegalArgumentException("nutrition must be >= 0");
        }
        if (saturationModifier < 0.0F) {
            throw new IllegalArgumentException("saturationModifier must be >= 0");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static FoodSpec of(int nutrition, float saturationModifier) {
        return builder().nutrition(nutrition).saturationModifier(saturationModifier).build();
    }

    public int nutrition() {
        return nutrition;
    }

    public float saturationModifier() {
        return saturationModifier;
    }

    public boolean alwaysEdible() {
        return alwaysEdible;
    }

    public boolean fast() {
        return fast;
    }

    public boolean meat() {
        return meat;
    }

    public static final class Builder {
        private int nutrition;
        private float saturationModifier;
        private boolean alwaysEdible;
        private boolean fast;
        private boolean meat;

        private Builder() {}

        public Builder nutrition(int nutrition) {
            this.nutrition = nutrition;
            return this;
        }

        public Builder saturationModifier(float saturationModifier) {
            this.saturationModifier = saturationModifier;
            return this;
        }

        public Builder alwaysEdible() {
            this.alwaysEdible = true;
            return this;
        }

        public Builder fast() {
            this.fast = true;
            return this;
        }

        public Builder meat() {
            this.meat = true;
            return this;
        }

        public FoodSpec build() {
            return new FoodSpec(this);
        }
    }
}
