package space.nows.mcnows.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Small per-mod config file helper backed by the game directory. */
public final class NowsConfigFiles {
    private final Path directory;

    public NowsConfigFiles(Path directory) {
        this.directory = directory;
    }

    public Path directory() {
        return directory;
    }

    public Path modDirectory(String modId) {
        return directory.resolve(modId);
    }

    public Path file(String modId, String name) {
        String fileName = name.endsWith(".properties") ? name : name + ".properties";
        return modDirectory(modId).resolve(fileName);
    }

    public Properties loadProperties(String modId, String name) throws IOException {
        Path file = file(modId, name);
        Properties properties = new Properties();
        if (Files.isRegularFile(file)) {
            try (InputStream input = Files.newInputStream(file)) {
                properties.load(input);
            }
        }
        return properties;
    }

    public void saveProperties(String modId, String name, Properties properties, String comment) throws IOException {
        Path file = file(modId, name);
        Files.createDirectories(file.getParent());
        try (OutputStream output = Files.newOutputStream(file)) {
            properties.store(output, comment);
        }
    }
}
