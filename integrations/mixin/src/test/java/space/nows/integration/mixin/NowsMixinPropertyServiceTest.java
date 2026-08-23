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

package space.nows.integration.mixin;

import org.junit.jupiter.api.Test;
import org.spongepowered.asm.service.IPropertyKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NowsMixinPropertyServiceTest {
    @Test
    void trimsAndReusesResolvedKeysByValue() {
        NowsMixinPropertyService service = new NowsMixinPropertyService();
        IPropertyKey key = service.resolveKey(" nows.test ");

        service.setProperty(key, "value");

        assertEquals("value", service.getProperty(service.resolveKey("nows.test")));
        assertEquals("value", service.getPropertyString(service.resolveKey(" nows.test "), "fallback"));
    }

    @Test
    void rejectsInvalidKeys() {
        NowsMixinPropertyService service = new NowsMixinPropertyService();

        assertThrows(IllegalArgumentException.class, () -> service.resolveKey(" "));
        assertThrows(NullPointerException.class, () -> service.getProperty(null));
        assertThrows(NullPointerException.class, () -> service.setProperty(null, "value"));
        assertThrows(NullPointerException.class, () -> service.getPropertyString(null, "fallback"));
    }
}
