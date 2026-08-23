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

/** Stable tool item facts translated by each Minecraft adapter. */
public final class ToolSpec {
    private final ToolKind kind;
    private final ToolTier tier;
    private final float attackDamage;
    private final float attackSpeed;
    private final int durability;

    private ToolSpec(Builder builder) {
        this.kind = builder.kind;
        this.tier = builder.tier;
        this.attackDamage = builder.attackDamage;
        this.attackSpeed = builder.attackSpeed;
        this.durability = builder.durability;
        if (durability < 0) {
            throw new IllegalArgumentException("durability must be >= 0");
        }
    }

    public static Builder sword(ToolTier tier) {
        return new Builder(ToolKind.SWORD).tier(tier).attack(3.0F, -2.4F);
    }

    public static Builder builder(ToolKind kind) {
        return new Builder(kind);
    }

    public ToolKind kind() {
        return kind;
    }

    public ToolTier tier() {
        return tier;
    }

    public float attackDamage() {
        return attackDamage;
    }

    public float attackSpeed() {
        return attackSpeed;
    }

    public int durability() {
        return durability;
    }

    public enum ToolKind {
        SWORD,
        PICKAXE,
        AXE,
        SHOVEL,
        HOE
    }

    public enum ToolTier {
        WOOD,
        STONE,
        IRON,
        GOLD,
        DIAMOND,
        NETHERITE
    }

    public static final class Builder {
        private final ToolKind kind;
        private ToolTier tier = ToolTier.IRON;
        private float attackDamage;
        private float attackSpeed;
        private int durability;

        private Builder(ToolKind kind) {
            this.kind = kind == null ? ToolKind.SWORD : kind;
        }

        public Builder tier(ToolTier tier) {
            this.tier = tier == null ? ToolTier.IRON : tier;
            return this;
        }

        public Builder attack(float attackDamage, float attackSpeed) {
            this.attackDamage = attackDamage;
            this.attackSpeed = attackSpeed;
            return this;
        }

        public Builder durability(int durability) {
            this.durability = durability;
            return this;
        }

        public ToolSpec build() {
            return new ToolSpec(this);
        }
    }
}
