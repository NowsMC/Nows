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

package space.nows.platform.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NowsServicesTest {
    @Test
    void rejectsNullRegistrations() {
        NowsServices services = new NowsServices();

        assertThrows(NullPointerException.class, () -> services.register(null, "value"));
        assertThrows(NullPointerException.class, () -> services.register(String.class, null));
        assertThrows(NullPointerException.class, () -> services.find(null));
        assertThrows(NullPointerException.class, () -> services.require(null));
    }

    @Test
    void rejectsDuplicateRegistrations() {
        NowsServices services = new NowsServices();

        services.register(String.class, "first");

        assertThrows(IllegalStateException.class, () -> services.register(String.class, "second"));
        assertSame("first", services.require(String.class));
    }

    @Test
    void canRegisterOptionalServiceWithoutReplacingExistingOne() {
        NowsServices services = new NowsServices();

        assertTrue(services.registerIfAbsent(String.class, "first"));
        assertFalse(services.registerIfAbsent(String.class, "second"));

        assertSame("first", services.require(String.class));
    }

    @Test
    void canReplaceServiceWhenIntegrationOwnsTheDecision() {
        NowsServices services = new NowsServices();

        assertTrue(services.replace(String.class, "first").isEmpty());

        assertSame("first", services.replace(String.class, "second").orElseThrow());
        assertSame("second", services.require(String.class));
    }
}
