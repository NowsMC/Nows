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

/** Stable Nows block registration data, translated by each Minecraft adapter. */
public final class BlockSpec {
    private final String id;
    private final BlockMaterial material;
    private final float destroyTime;
    private final float explosionResistance;
    private final boolean requiresCorrectTool;
    private final boolean noOcclusion;
    private final BlockSound sound;
    private final MapColor mapColor;
    private final BlockRenderType renderType;
    private final LightSpec light;
    private final BlockShapeSpec shape;
    private final float friction;
    private final float speedFactor;
    private final float jumpFactor;
    private final ItemSpec item;

    private BlockSpec(Builder builder) {
        this.id = ItemSpec.requireId(builder.id);
        this.material = builder.material;
        this.destroyTime = builder.destroyTime;
        this.explosionResistance = builder.explosionResistance;
        this.requiresCorrectTool = builder.requiresCorrectTool;
        this.noOcclusion = builder.noOcclusion;
        this.sound = builder.sound;
        this.mapColor = builder.mapColor;
        this.renderType = builder.renderType;
        this.light = builder.light;
        this.shape = builder.shape;
        this.friction = builder.friction;
        this.speedFactor = builder.speedFactor;
        this.jumpFactor = builder.jumpFactor;
        this.item = builder.item;
        if (destroyTime < 0.0F) {
            throw new IllegalArgumentException("destroyTime must be >= 0");
        }
        if (explosionResistance < 0.0F) {
            throw new IllegalArgumentException("explosionResistance must be >= 0");
        }
        if (friction < 0.0F || speedFactor < 0.0F || jumpFactor < 0.0F) {
            throw new IllegalArgumentException("movement factors must be >= 0");
        }
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public String id() {
        return id;
    }

    public BlockMaterial material() {
        return material;
    }

    public float destroyTime() {
        return destroyTime;
    }

    public float explosionResistance() {
        return explosionResistance;
    }

    public boolean requiresCorrectTool() {
        return requiresCorrectTool;
    }

    public boolean noOcclusion() {
        return noOcclusion;
    }

    public BlockSound sound() {
        return sound;
    }

    public MapColor mapColor() {
        return mapColor;
    }

    public BlockRenderType renderType() {
        return renderType;
    }

    public LightSpec light() {
        return light;
    }

    public BlockShapeSpec shape() {
        return shape;
    }

    public float friction() {
        return friction;
    }

    public float speedFactor() {
        return speedFactor;
    }

    public float jumpFactor() {
        return jumpFactor;
    }

    public ItemSpec item() {
        return item;
    }

    public static final class Builder {
        private final String id;
        private BlockMaterial material = BlockMaterial.STONE;
        private float destroyTime = 1.0F;
        private float explosionResistance = 1.0F;
        private boolean requiresCorrectTool;
        private boolean noOcclusion;
        private BlockSound sound;
        private MapColor mapColor;
        private BlockRenderType renderType = BlockRenderType.SOLID;
        private LightSpec light = LightSpec.none();
        private BlockShapeSpec shape = BlockShapeSpec.FULL_CUBE;
        private float friction = 0.6F;
        private float speedFactor = 1.0F;
        private float jumpFactor = 1.0F;
        private ItemSpec item;

        private Builder(String id) {
            this.id = id;
        }

        public Builder material(BlockMaterial material) {
            this.material = material == null ? BlockMaterial.STONE : material;
            return this;
        }

        public Builder strength(float destroyTime, float explosionResistance) {
            this.destroyTime = destroyTime;
            this.explosionResistance = explosionResistance;
            return this;
        }

        public Builder requiresCorrectTool() {
            this.requiresCorrectTool = true;
            return this;
        }

        public Builder noOcclusion() {
            this.noOcclusion = true;
            return this;
        }

        public Builder sound(BlockSound sound) {
            this.sound = sound;
            return this;
        }

        public Builder mapColor(MapColor mapColor) {
            this.mapColor = mapColor;
            return this;
        }

        public Builder renderType(BlockRenderType renderType) {
            this.renderType = renderType == null ? BlockRenderType.SOLID : renderType;
            return this;
        }

        public Builder light(LightSpec light) {
            this.light = light == null ? LightSpec.none() : light;
            return this;
        }

        public Builder shape(BlockShapeSpec shape) {
            this.shape = shape == null ? BlockShapeSpec.FULL_CUBE : shape;
            return this;
        }

        public Builder friction(float friction) {
            this.friction = friction;
            return this;
        }

        public Builder speedFactor(float speedFactor) {
            this.speedFactor = speedFactor;
            return this;
        }

        public Builder jumpFactor(float jumpFactor) {
            this.jumpFactor = jumpFactor;
            return this;
        }

        public Builder item(ItemSpec item) {
            this.item = item;
            return this;
        }

        public BlockSpec build() {
            return new BlockSpec(this);
        }
    }
}
