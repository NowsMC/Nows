package space.nows.mcnows.minecraft;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record LaunchArguments(
        String minecraftVersion,
        Path minecraftDirectory,
        Path gameDirectory,
        List<String> minecraftArguments
) {
    public static LaunchArguments parse(String[] args) {
        String minecraftVersion = null;
        Path minecraftDirectory = null;
        Path gameDirectory = Path.of(".").toAbsolutePath().normalize();
        List<String> forwarded = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--nowsMinecraftVersion") && i + 1 < args.length) {
                minecraftVersion = args[++i];
                continue;
            }
            if (arg.equals("--gameDir") && i + 1 < args.length) {
                String value = args[++i];
                gameDirectory = Path.of(value).toAbsolutePath().normalize();
                forwarded.add(arg); forwarded.add(value); continue;
            }
            if (arg.equals("--assetsDir") && i + 1 < args.length) {
                String value = args[++i];
                Path assetsDirectory = Path.of(value).toAbsolutePath().normalize();
                minecraftDirectory = assetsDirectory.getParent();
                forwarded.add(arg); forwarded.add(value); continue;
            }
            forwarded.add(arg);
        }
        if (minecraftVersion == null || minecraftVersion.isBlank()) minecraftVersion = System.getProperty("nows.minecraftVersion");
        if (minecraftVersion == null || minecraftVersion.isBlank()) throw new IllegalArgumentException("Missing --nowsMinecraftVersion <version>");
        if (minecraftDirectory == null) {
            minecraftDirectory = inferMinecraftDirectory(gameDirectory);
        }
        return new LaunchArguments(minecraftVersion, minecraftDirectory, gameDirectory, List.copyOf(forwarded));
    }

    private static Path inferMinecraftDirectory(Path gameDirectory) {
        Path normalized = gameDirectory.toAbsolutePath().normalize();
        Path fileName = normalized.getFileName();
        Path profiles = normalized.getParent();
        Path nows = profiles == null ? null : profiles.getParent();
        Path minecraft = nows == null ? null : nows.getParent();
        if (fileName != null && profiles != null && nows != null && minecraft != null
                && profiles.getFileName() != null
                && nows.getFileName() != null
                && profiles.getFileName().toString().equals("profiles")
                && nows.getFileName().toString().equals("nows")) {
            return minecraft.toAbsolutePath().normalize();
        }
        return Path.of(".").toAbsolutePath().normalize();
    }
}
