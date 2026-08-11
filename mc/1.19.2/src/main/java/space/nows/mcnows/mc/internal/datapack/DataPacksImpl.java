package space.nows.mcnows.mc.internal.datapack;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import space.nows.mcnows.mc.api.datapack.DataPacks;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class DataPacksImpl implements DataPacks {
    private final Path gameDirectory;
    private final Map<PackType, List<RepositorySource>> sources = new EnumMap<>(PackType.class);

    public DataPacksImpl(Path gameDirectory) {
        this.gameDirectory = gameDirectory;
    }

    @Override
    public Path gameDirectory() {
        return gameDirectory;
    }

    @Override
    public Path nowsPackDirectory() {
        return gameDirectory.resolve("nows").resolve("packs");
    }

    @Override
    public Path modPackDirectory(String modId) {
        return nowsPackDirectory().resolve(modId);
    }

    @Override
    public void registerSource(PackType type, RepositorySource source) {
        sources.computeIfAbsent(type, ignored -> new ArrayList<>()).add(source);
    }

    @Override
    public List<RepositorySource> sources(PackType type) {
        return List.copyOf(sources.getOrDefault(type, List.of()));
    }

    @Override
    public PackRepository repository(PackType type) {
        return new PackRepository(type, sources(type).toArray(RepositorySource[]::new));
    }
}
