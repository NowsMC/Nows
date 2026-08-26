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
import space.nows.platform.api.NowsSide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LaunchArgumentsTest {
    @AfterEach
    void clearProperties() {
        System.clearProperty("nows.minecraftVersion");
        System.clearProperty("nows.profileId");
        System.clearProperty("nows.side");
    }

    @Test
    void readsNowsProfileIdFromSystemPropertyForInstanceLaunchers() {
        System.setProperty("nows.minecraftVersion", "1.20.1");
        System.setProperty("nows.profileId", "nows-0.9.0-1.20.1");

        LaunchArguments arguments = LaunchArguments.parse(new String[] {
                "--gameDir", "instance/.minecraft"
        });

        assertEquals("1.20.1", arguments.minecraftVersion());
        assertEquals("nows-0.9.0-1.20.1", arguments.profileId());
        assertEquals(NowsSide.CLIENT, arguments.side());
    }

    @Test
    void readsServerSideFromSystemProperty() {
        System.setProperty("nows.minecraftVersion", "1.20.1");
        System.setProperty("nows.side", "server");

        LaunchArguments arguments = LaunchArguments.parse(new String[] {
                "--nowsGameDir", "server"
        });

        assertEquals(NowsSide.SERVER, arguments.side());
        assertEquals(0, arguments.minecraftArguments().size());
    }

    @Test
    void readsServerSideFromNowsArgumentWithoutForwardingIt() {
        LaunchArguments arguments = LaunchArguments.parse(new String[] {
                "--nowsMinecraftVersion", "1.20.1",
                "--nowsSide", "server",
                "nogui"
        });

        assertEquals(NowsSide.SERVER, arguments.side());
        assertEquals(1, arguments.minecraftArguments().size());
        assertEquals("nogui", arguments.minecraftArguments().get(0));
    }

    @Test
    void rejectsBothAsPhysicalRuntimeSide() {
        assertThrows(IllegalArgumentException.class, () -> LaunchArguments.parse(new String[] {
                "--nowsMinecraftVersion", "1.20.1",
                "--nowsSide", "both"
        }));
    }
}
