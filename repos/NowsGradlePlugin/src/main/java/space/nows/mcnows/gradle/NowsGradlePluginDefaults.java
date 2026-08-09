package space.nows.mcnows.gradle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

final class NowsGradlePluginDefaults {
    private NowsGradlePluginDefaults() {
    }

    static String nowsVersion() {
        Properties defaults = new Properties();
        try (InputStream input = NowsGradlePluginDefaults.class.getResourceAsStream(
                "/META-INF/nows/gradle-plugin/defaults.properties")) {
            if (input != null) {
                defaults.load(input);
            }
        } catch (IOException ignored) {
            return "development";
        }
        return defaults.getProperty("nows.version", "development").trim();
    }
}
