package space.nows.mcnows.minecraft;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;

/** Minimal bootstrap used only by this monorepo's build/example. */
public final class PrepareMinecraftCli {
    private PrepareMinecraftCli() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: PrepareMinecraftCli <minecraft-version> <output-jar>");
        }
        String version = args[0];
        Path output = Path.of(args[1]).toAbsolutePath().normalize();
        Path cacheDir = output.getParent();
        Files.createDirectories(cacheDir);

        MojangMetadata.VersionDownloads downloads = MojangMetadata.resolve(version);
        Path raw = MojangMetadata.downloadCached(downloads.client(), cacheDir.resolve("client-runtime.jar"));
        try (JarFile jar = new JarFile(raw.toFile())) {
            if (jar.getEntry("net/minecraft/client/Minecraft.class") == null) {
                throw new IllegalStateException(
                        "Minecraft " + version + " is not Mojang-named. "
                                + "Use NowsGradlePlugin, which owns legacy remapping support.");
            }
        }
        Files.copy(raw, output, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Nows: prepared Mojang-named Minecraft " + version + " at " + output);
    }
}
