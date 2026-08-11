package space.nows.mcnows.mc.internal;

import net.minecraft.server.packs.repository.RepositorySource;
import space.nows.mcnows.core.mod.ModContainer;
import space.nows.mcnows.mc.internal.resources.ModPackSource;

import java.util.List;
import java.util.Set;

public final class ClientHooks {
    private static volatile String nowsVersion = "development";
    private static volatile String minecraftVersion = "unknown";
    private static volatile int modCount;
    private static volatile List<ModContainer> mods = List.of();

    private ClientHooks() {
    }

    public static void configure(String nows, String minecraft, int mods) {
        nowsVersion = nows;
        minecraftVersion = minecraft;
        modCount = mods;
    }

    public static void configure(String nows, String minecraft, int mods, List<ModContainer> modContainers) {
        configure(nows, minecraft, mods);
        ClientHooks.mods = List.copyOf(modContainers);
    }

    public static Set<RepositorySource> withModResourcePackSource(Set<RepositorySource> sources, RepositorySource[] originalSources) {
        return ModPackSource.appendClientSource(sources, originalSources, mods);
    }

    public static String loaderLine() {
        return "Nows Loader " + nowsVersion;
    }

    public static String modLine() {
        return modCount + " Nows mod" + (modCount == 1 ? "" : "s") + " loaded";
    }
}
