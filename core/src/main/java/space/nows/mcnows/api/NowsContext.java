package space.nows.mcnows.api;

import space.nows.mcnows.core.mod.ModContainer;
import space.nows.mcnows.core.mod.ModDescriptor;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

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

    public boolean isModLoaded(String id) {
        return mod(id).isPresent();
    }

    public Optional<ModContainer> mod(String id) {
        return mods.stream()
                .filter(mod -> mod.descriptor().id().equals(id))
                .findFirst();
    }

    public ModContainer requireMod(String id) {
        return mod(id).orElseThrow(() -> new NoSuchElementException("Nows mod '" + id + "' is not loaded"));
    }

    public Optional<ModDescriptor> modDescriptor(String id) {
        return mod(id).map(ModContainer::descriptor);
    }

    public ModDescriptor requireModDescriptor(String id) {
        return requireMod(id).descriptor();
    }

    public List<ModDescriptor> modDescriptors() {
        return mods.stream().map(ModContainer::descriptor).toList();
    }

    public Map<String, ModContainer> modsById() {
        Map<String, ModContainer> byId = new LinkedHashMap<>();
        for (ModContainer mod : mods) {
            byId.put(mod.descriptor().id(), mod);
        }
        return Map.copyOf(byId);
    }
}
