package space.nows.mcnows.mc.api;

import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.mc.api.client.ui.NowsUi;
import space.nows.mcnows.mc.api.command.NowsCommands;
import space.nows.mcnows.mc.api.datapack.NowsDataPacks;
import space.nows.mcnows.mc.api.datagen.NowsDataGen;
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

    public static NowsCommands commands(NowsContext context) {
        return context.service(NowsCommands.class);
    }

    public static NowsDataGen dataGen(NowsContext context) {
        return context.service(NowsDataGen.class);
    }

    public static NowsUi ui(NowsContext context) {
        return context.service(NowsUi.class);
    }
}
