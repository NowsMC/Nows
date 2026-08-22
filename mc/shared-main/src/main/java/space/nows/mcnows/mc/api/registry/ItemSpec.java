package space.nows.mcnows.mc.api.registry;

import java.util.Objects;

/** Stable Nows item registration data, translated by each Minecraft adapter. */
public final class ItemSpec {
    private final String id;
    private final int maxStackSize;
    private final boolean fireResistant;

    private ItemSpec(Builder builder) {
        this.id = requireId(builder.id);
        this.maxStackSize = builder.maxStackSize;
        this.fireResistant = builder.fireResistant;
        if (maxStackSize < 1 || maxStackSize > 99) {
            throw new IllegalArgumentException("maxStackSize must be between 1 and 99");
        }
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public String id() {
        return id;
    }

    public int maxStackSize() {
        return maxStackSize;
    }

    public boolean fireResistant() {
        return fireResistant;
    }

    static String requireId(String id) {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return id;
    }

    public static final class Builder {
        private final String id;
        private int maxStackSize = 64;
        private boolean fireResistant;

        private Builder(String id) {
            this.id = id;
        }

        public Builder maxStackSize(int maxStackSize) {
            this.maxStackSize = maxStackSize;
            return this;
        }

        public Builder fireResistant() {
            this.fireResistant = true;
            return this;
        }

        public ItemSpec build() {
            return new ItemSpec(this);
        }
    }
}
