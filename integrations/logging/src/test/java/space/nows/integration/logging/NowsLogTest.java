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

package space.nows.integration.logging;

import org.junit.jupiter.api.Test;
import reactor.util.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NowsLogTest {
    @Test
    void createsNamedLoggers() {
        assertNotNull(NowsLog.get(" space.nows.test "));
        assertNotNull(NowsLog.get(NowsLogTest.class));
    }

    @Test
    void rejectsInvalidLoggerInputs() {
        assertThrows(NullPointerException.class, () -> NowsLog.get((Class<?>) null));
        assertThrows(IllegalArgumentException.class, () -> NowsLog.get(" "));
        assertThrows(NullPointerException.class, () -> NowsLog.phase(null, "phase"));
        Logger logger = NowsLog.get(NowsLogTest.class);
        assertThrows(IllegalArgumentException.class, () -> NowsLog.phase(logger, ""));
    }
}
