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

package space.nows.mc.internal.client.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.junit.jupiter.api.Test;
import space.nows.mc.api.client.keybind.KeybindRegistration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class KeybindApiImplContractTest {
    @Test
    void createsNativeKeyboardMappingWithThisMinecraftVersionConstructor() {
        KeyMapping mapping = KeybindApiImpl.createKeyboardMapping(
                "key.nows.contract",
                "key.categories.nows",
                65);

        assertNotNull(mapping);
        assertEquals("key.nows.contract", mapping.getName());
    }

    @Test
    void categoryBasedMinecraftVersionsReuseCustomCategoryWhenRegisteringKeyboard() {
        assumeTrue(hasCategoryBasedKeyMappingConstructor());

        KeybindApiImpl api = new KeybindApiImpl();
        String suffix = Long.toUnsignedString(System.nanoTime(), 36);
        String category = "nows:keybind_contract_" + suffix;
        api.registerCategory(category);

        KeybindRegistration registration = assertDoesNotThrow(() ->
                api.registerKeyboard("key.nows.contract." + suffix, category, 65));

        assertEquals(category, registration.category());
    }

    @Test
    void categoryBasedMinecraftVersionsReuseDefaultCategoryAcrossApiInstances() {
        assumeTrue(hasCategoryBasedKeyMappingConstructor());

        assertDoesNotThrow(KeybindApiImpl::new);
        assertDoesNotThrow(KeybindApiImpl::new);
    }

    private static boolean hasCategoryBasedKeyMappingConstructor() {
        try {
            Class<?> categoryType = Class.forName("net.minecraft.client.KeyMapping$Category");
            KeyMapping.class.getConstructor(String.class, InputConstants.Type.class, int.class, categoryType);
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            return false;
        }
    }
}
