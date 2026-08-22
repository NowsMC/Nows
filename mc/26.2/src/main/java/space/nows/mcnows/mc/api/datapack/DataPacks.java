package space.nows.mcnows.mc.api.datapack;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import space.nows.mcnows.mc.api.datapack.PackTarget;

import java.nio.file.Path;
import java.util.List;

/** Version-backed datapack and resource-pack source registry. */
public interface DataPacks {
    Path gameDirectory();

    Path nowsPackDirectory();

    Path modPackDirectory(String modId);

    default Path modAssetsDirectory(String modId) {
        return modPackDirectory(modId).resolve("assets").resolve(modId);
    }

    default Path modDataDirectory(String modId) {
        return modPackDirectory(modId).resolve("data").resolve(modId);
    }


    void registerSource(PackType type, RepositorySource source);

    default void registerSource(PackTarget target, Object source) {
        registerSource(packType(target), (RepositorySource) source);
    }


    List<RepositorySource> sources(PackType type);

    default List<Object> sources(PackTarget target) {
        return List.copyOf(sources(packType(target)));
    }


    PackRepository repository(PackType type);

    default Object repository(PackTarget target) {
        return repository(packType(target));
    }

    private static PackType packType(PackTarget target) {
        return target == PackTarget.CLIENT_RESOURCES ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA;
    }

}
