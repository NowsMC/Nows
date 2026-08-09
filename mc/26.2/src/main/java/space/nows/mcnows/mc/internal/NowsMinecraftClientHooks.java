package space.nows.mcnows.mc.internal;

public final class NowsMinecraftClientHooks {
    private static volatile String nowsVersion = "development";
    private static volatile String minecraftVersion = "26.2";
    private static volatile int modCount;

    private NowsMinecraftClientHooks() {
    }

    public static void configure(String nows, String minecraft, int mods) {
        nowsVersion = nows;
        minecraftVersion = minecraft;
        modCount = mods;
    }

    public static String loaderLine() {
        return "Nows Loader " + nowsVersion;
    }

    public static String modLine() {
        return modCount + " Nows mod" + (modCount == 1 ? "" : "s") + " loaded";
    }
}
