package space.nows.mcnows.mc.internal;

import space.nows.mcnows.api.NowsServices;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.mc.api.client.player.NowsPlayerApi;
import space.nows.mcnows.mc.api.client.ui.NowsUi;
import space.nows.mcnows.mc.api.command.NowsCommands;
import space.nows.mcnows.mc.api.datapack.NowsDataPacks;
import space.nows.mcnows.mc.api.datagen.NowsDataGen;
import space.nows.mcnows.mc.api.registry.NowsRegistryApi;
import space.nows.mcnows.mc.internal.client.player.NowsPlayerApiImpl;
import space.nows.mcnows.mc.internal.client.NowsLoaderMenu;
import space.nows.mcnows.mc.internal.client.NowsUiImpl;
import space.nows.mcnows.mc.internal.command.NowsCommandsImpl;
import space.nows.mcnows.mc.internal.datapack.NowsDataPacksImpl;
import space.nows.mcnows.mc.internal.datagen.NowsDataGenImpl;
import space.nows.mcnows.mc.internal.registry.NowsRegistryApiImpl;

import java.nio.file.Path;

/** Installs Minecraft-version-backed services into the loader context. */
public final class NowsMinecraftIntegration {
    private NowsMinecraftIntegration() {}

    public static void install(NowsServices services, Path gameDirectory) {
        services.register(NowsRegistryApi.class, new NowsRegistryApiImpl());
        services.register(NowsDataPacks.class, new NowsDataPacksImpl(gameDirectory));
        services.register(NowsCommands.class, new NowsCommandsImpl());
        services.register(NowsDataGen.class, new NowsDataGenImpl(gameDirectory.resolve("nows").resolve("generated")));
        services.register(NowsUi.class, NowsUiImpl.INSTANCE);
        services.register(NowsPlayerApi.class, new NowsPlayerApiImpl());
    }

    public static void installBuiltInUi(NowsContext context) {
        NowsLoaderMenu.install(context);
    }
}
