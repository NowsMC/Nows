package space.nows.mcnows.minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/** Version-specific Minecraft launch policy loaded from mc/<minecraft-version>. */
public record MinecraftVersionPolicy(
        String minecraftVersion,
        String clientMainClass,
        List<String> builtInMixinConfigs,
        String resourcePath,
        boolean bundled) {
    private static final String DEFAULT_CLIENT_MAIN_CLASS = "net.minecraft.client.main.Main";

    public static MinecraftVersionPolicy load(String minecraftVersion) throws IOException {
        String normalized = normalizeVersion(minecraftVersion);
        String resourcePath = "META-INF/nows/mc/" + normalized + "/nows-minecraft.properties";

        Properties properties = new Properties();
        ClassLoader loader = MinecraftVersionPolicy.class.getClassLoader();
        try (InputStream input = loader.getResourceAsStream(resourcePath)) {
            if (input == null) {
                return new MinecraftVersionPolicy(normalized, DEFAULT_CLIENT_MAIN_CLASS, List.of(), resourcePath, false);
            }
            properties.load(input);
        }

        String declaredVersion = read(properties, "minecraft.version", normalized);
        if (!declaredVersion.equals(normalized)) {
            throw new IOException(resourcePath + " declares minecraft.version=" + declaredVersion
                    + " but launcher requested " + normalized);
        }

        return new MinecraftVersionPolicy(
                normalized,
                read(properties, "client.mainClass", DEFAULT_CLIENT_MAIN_CLASS),
                readList(properties, "runtime.builtinMixinConfigs"),
                resourcePath,
                true);
    }

    private static String normalizeVersion(String minecraftVersion) {
        String normalized = Objects.requireNonNull(minecraftVersion, "minecraftVersion").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Minecraft version is blank");
        }
        if (normalized.indexOf('/') >= 0 || normalized.indexOf('\\') >= 0) {
            throw new IllegalArgumentException("Minecraft version must not contain path separators: " + normalized);
        }
        return normalized;
    }

    private static String read(Properties properties, String key, String fallback) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private static List<String> readList(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split(",")).stream()
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}
