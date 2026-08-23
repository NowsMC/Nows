package space.nows.mcnows.minecraft;

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
        System.setProperty("nows.profileId", "nows-0.6.2-1.20.1");

        LaunchArguments arguments = LaunchArguments.parse(new String[] {
                "--gameDir", "instance/.minecraft"
        });

        assertEquals("1.20.1", arguments.minecraftVersion());
        assertEquals("nows-0.6.2-1.20.1", arguments.profileId());
    }
}
