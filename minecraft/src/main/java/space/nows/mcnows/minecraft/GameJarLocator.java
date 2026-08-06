package space.nows.mcnows.minecraft;

import java.io.IOException;
import java.nio.file.Path;
import java.util.jar.JarFile;

public final class GameJarLocator {
    private GameJarLocator() {}

    public static Path locateClientJar() throws IOException {
        for (String element : System.getProperty("java.class.path", "").split(java.io.File.pathSeparator)) {
            Path path = Path.of(element);
            if (!java.nio.file.Files.isRegularFile(path) || !element.endsWith(".jar")) continue;
            try (JarFile jar = new JarFile(path.toFile())) {
                if (jar.getEntry("net/minecraft/client/main/Main.class") != null) return path.toAbsolutePath().normalize();
            } catch (IOException ignored) { }
        }
        throw new IOException("Could not locate the Minecraft client jar on java.class.path");
    }
}
