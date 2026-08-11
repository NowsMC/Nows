package space.nows.mcnows.mc.api.datapack;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;

import java.nio.file.Path;
import java.util.List;

/** Version-backed datapack and resource-pack source registry. */
public interface DataPacks {
    Path gameDirectory();

    Path nowsPackDirectory();

    Path modPackDirectory(String modId);

    void registerSource(PackType type, RepositorySource source);

    List<RepositorySource> sources(PackType type);

    PackRepository repository(PackType type);
}
