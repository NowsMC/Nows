package space.nows.mcnows.api;

import space.nows.mcnows.core.mod.ModContainer;

import java.nio.file.Path;
import java.util.List;

/** Stable runtime context. Optional functionality lives in {@link NowsServices}. */
public record NowsContext(
        String minecraftVersion,
        Path gameDirectory,
        List<ModContainer> mods,
        ClassLoader gameClassLoader,
        NowsServices services
) {
    public NowsContext {
        mods = List.copyOf(mods);
    }

    public <T> T service(Class<T> type) {
        return services.require(type);
    }
}
