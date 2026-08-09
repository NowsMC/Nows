package space.nows.mcnows.minecraft;

import space.nows.mcnows.api.NowsSide;
import space.nows.mcnows.core.mod.ModContainer;

import java.io.IOException;
import java.util.List;

/** Minecraft-version policy stays outside core because it is expected to evolve. */
public final class MinecraftCompatibility {
    private MinecraftCompatibility() {}

    public static void validate(List<ModContainer> mods, String minecraftVersion) throws IOException {
        validate(mods, minecraftVersion, NowsSide.CLIENT);
    }

    public static void validate(List<ModContainer> mods, String minecraftVersion, NowsSide runtimeSide) throws IOException {
        for (ModContainer mod : mods) {
            String requested = mod.descriptor().minecraft();
            if (!requested.equals("*") && !requested.equals(minecraftVersion)) {
                throw new IOException("Mod " + mod.descriptor().id() + " targets Minecraft " + requested
                        + " but this profile is " + minecraftVersion);
            }
            if (!mod.descriptor().side().supports(runtimeSide)) {
                throw new IOException("Mod " + mod.descriptor().id() + " targets side "
                        + mod.descriptor().side().metadataName() + " but this runtime is "
                        + runtimeSide.metadataName());
            }
        }
    }
}
