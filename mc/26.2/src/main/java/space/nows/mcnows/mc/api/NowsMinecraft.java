package space.nows.mcnows.mc.api;

import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.mc.api.datapack.NowsDataPacks;
import space.nows.mcnows.mc.api.registry.NowsRegistryApi;

/** Entry point for Minecraft-version-backed Nows APIs. */
public final class NowsMinecraft {
    private NowsMinecraft() {}

    public static NowsRegistryApi registries(NowsContext context) {
        return context.service(NowsRegistryApi.class);
    }

    public static NowsDataPacks dataPacks(NowsContext context) {
        return context.service(NowsDataPacks.class);
    }
}
