package space.nows.mcnows.mc.internal.resources;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.flag.FeatureFlagSet;
import space.nows.mcnows.core.mod.ModContainer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.ZipFile;

public final class ModPackSource implements RepositorySource {
    private static final int RESOURCE_PACK_FORMAT_1_20_1 = 15;
    private static final Pack.Info INFO = new Pack.Info(
            Component.literal("Nows mod resources"),
            RESOURCE_PACK_FORMAT_1_20_1,
            FeatureFlagSet.of());

    private final List<ModContainer> mods;

    private ModPackSource(List<ModContainer> mods) {
        this.mods = mods;
    }

    public static Set<RepositorySource> appendClientSource(
            Set<RepositorySource> sources,
            RepositorySource[] originalSources,
            List<ModContainer> mods) {
        int resourceMods = countResourceMods(mods);
        if (resourceMods == 0 || !isClientResourceRepository(originalSources)) {
            return sources;
        }
        if (sources.stream().anyMatch(ModPackSource.class::isInstance)) {
            return sources;
        }
        LinkedHashSet<RepositorySource> updated = new LinkedHashSet<>(sources);
        updated.add(new ModPackSource(List.copyOf(mods)));
        System.out.println("[Nows] Added mod resource pack source for " + resourceMods + " mod(s)");
        return Collections.unmodifiableSet(updated);
    }

    @Override
    public void loadPacks(Consumer<Pack> output) {
        for (ModContainer mod : mods) {
            if (!hasResources(mod.path())) {
                continue;
            }
            String id = "nows/" + mod.descriptor().id();
            System.out.println("[Nows] Loading mod resource pack: " + mod.descriptor().id());
            output.accept(Pack.create(
                    id,
                    Component.literal(mod.descriptor().name()),
                    true,
                    packId -> open(packId, mod.path()),
                    INFO,
                    PackType.CLIENT_RESOURCES,
                    Pack.Position.TOP,
                    true,
                    PackSource.BUILT_IN));
        }
    }

    private static int countResourceMods(List<ModContainer> mods) {
        int count = 0;
        for (ModContainer mod : mods) {
            if (hasResources(mod.path())) {
                count++;
            }
        }
        return count;
    }

    private static boolean isClientResourceRepository(RepositorySource[] sources) {
        for (RepositorySource source : sources) {
            if (source.getClass().getName().equals("net.minecraft.client.resources.ClientPackSource")) {
                return true;
            }
        }
        return false;
    }

    private static PackResources open(String id, Path jar) {
        try {
            return new ModPackResources(id, jar);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open Nows mod resource pack " + jar, e);
        }
    }

    private static boolean hasResources(Path jar) {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            return zip.stream().anyMatch(entry -> {
                String name = entry.getName();
                return !entry.isDirectory() && (name.startsWith("assets/") || name.startsWith("data/"));
            });
        } catch (IOException e) {
            return false;
        }
    }
}
