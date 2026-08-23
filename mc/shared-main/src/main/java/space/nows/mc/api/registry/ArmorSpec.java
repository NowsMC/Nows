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

/** Stable armor item facts translated by each Minecraft adapter. */
public record ArmorSpec(ArmorMaterial material, EquipmentSlot slot) {
    public ArmorSpec {
        if (material == null) {
            material = ArmorMaterial.IRON;
        }
        if (slot == null) {
            slot = EquipmentSlot.CHEST;
        }
    }

    public static ArmorSpec of(ArmorMaterial material, EquipmentSlot slot) {
        return new ArmorSpec(material, slot);
    }

    public enum ArmorMaterial {
        LEATHER,
        CHAINMAIL,
        IRON,
        GOLD,
        DIAMOND,
        NETHERITE
    }
}
