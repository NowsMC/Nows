package space.nows.mcnows.mc.internal;

import space.nows.mcnows.core.mod.ModContainer;

import java.util.List;

public final class NowsMinecraftClientHooks {
    private static volatile String nowsVersion = "development";
    private static volatile String minecraftVersion = "1.20.1";
    private static volatile int modCount;

    private NowsMinecraftClientHooks() {
    }

    public static void configure(String nows, String minecraft, int mods) {
        nowsVersion = nows;
        minecraftVersion = minecraft;
        modCount = mods;
    }

    public static void configure(String nows, String minecraft, int mods, List<ModContainer> modContainers) {
        configure(nows, minecraft, mods);
    }

    public static String loaderLine() {
        return "Nows Loader " + nowsVersion;
    }

    public static String modLine() {
        return modCount + " Nows mod" + (modCount == 1 ? "" : "s") + " loaded";
    }
}
