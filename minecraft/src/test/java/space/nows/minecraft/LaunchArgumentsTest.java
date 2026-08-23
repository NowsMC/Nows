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

package space.nows.minecraft;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LaunchArgumentsTest {
    @AfterEach
    void clearProperties() {
        System.clearProperty("nows.minecraftVersion");
        System.clearProperty("nows.profileId");
    }

    @Test
    void readsNowsProfileIdFromSystemPropertyForInstanceLaunchers() {
        System.setProperty("nows.minecraftVersion", "1.20.1");
        System.setProperty("nows.profileId", "nows-0.8.0-1.20.1");

        LaunchArguments arguments = LaunchArguments.parse(new String[] {
                "--gameDir", "instance/.minecraft"
        });

        assertEquals("1.20.1", arguments.minecraftVersion());
        assertEquals("nows-0.8.0-1.20.1", arguments.profileId());
    }
}
