package space.nows.mcnows.minecraft;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record LaunchArguments(String minecraftVersion, Path gameDirectory, List<String> minecraftArguments) {
    public static LaunchArguments parse(String[] args) {
        String minecraftVersion = null;
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
            forwarded.add(arg);
        }
        if (minecraftVersion == null || minecraftVersion.isBlank()) minecraftVersion = System.getProperty("nows.minecraftVersion");
        if (minecraftVersion == null || minecraftVersion.isBlank()) throw new IllegalArgumentException("Missing --nowsMinecraftVersion <version>");
        return new LaunchArguments(minecraftVersion, gameDirectory, List.copyOf(forwarded));
    }
}
