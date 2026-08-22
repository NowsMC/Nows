package space.nows.mcnows.mc.api.registry;

/** Stable Nows block registration data, translated by each Minecraft adapter. */
public final class BlockSpec {
    private final String id;
    private final BlockMaterial material;
    private final float destroyTime;
    private final float explosionResistance;
    private final boolean requiresCorrectTool;
    private final boolean noOcclusion;
    private final ItemSpec item;

    private BlockSpec(Builder builder) {
        this.id = ItemSpec.requireId(builder.id);
        this.material = builder.material;
        this.destroyTime = builder.destroyTime;
        this.explosionResistance = builder.explosionResistance;
        this.requiresCorrectTool = builder.requiresCorrectTool;
        this.noOcclusion = builder.noOcclusion;
        this.item = builder.item;
        if (destroyTime < 0.0F) {
            throw new IllegalArgumentException("destroyTime must be >= 0");
        }
        if (explosionResistance < 0.0F) {
            throw new IllegalArgumentException("explosionResistance must be >= 0");
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

        public Builder item(ItemSpec item) {
            this.item = item;
            return this;
        }

        public BlockSpec build() {
            return new BlockSpec(this);
        }
    }
}
