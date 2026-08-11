package space.nows.mcnows.mc.internal;

import space.nows.mcnows.api.NowsServices;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.mc.api.client.config.ConfigUi;
import space.nows.mcnows.mc.api.client.player.PlayerApi;
import space.nows.mcnows.mc.api.client.ui.Ui;
import space.nows.mcnows.mc.api.command.CommandApi;
import space.nows.mcnows.mc.api.datapack.DataPacks;
import space.nows.mcnows.mc.api.datagen.DataGen;
import space.nows.mcnows.mc.api.event.GameEvents;
import space.nows.mcnows.mc.api.registry.RegistryApi;
import space.nows.mcnows.mc.internal.client.config.ConfigUiImpl;
import space.nows.mcnows.mc.internal.client.player.PlayerApiImpl;
import space.nows.mcnows.mc.internal.client.LoaderMenu;
import space.nows.mcnows.mc.internal.client.UiImpl;
import space.nows.mcnows.mc.internal.command.CommandApiImpl;
import space.nows.mcnows.mc.internal.datapack.DataPacksImpl;
import space.nows.mcnows.mc.internal.datagen.DataGenImpl;
import space.nows.mcnows.mc.internal.event.GameEventsImpl;
import space.nows.mcnows.mc.internal.registry.RegistryApiImpl;

import java.nio.file.Path;

/** Installs Minecraft-version-backed services into the loader context. */
public final class MinecraftIntegration {
    private MinecraftIntegration() {}

    public static void install(NowsServices services, Path gameDirectory) {
        services.register(RegistryApi.class, new RegistryApiImpl());
        services.register(DataPacks.class, new DataPacksImpl(gameDirectory));
        services.register(CommandApi.class, new CommandApiImpl());
        services.register(DataGen.class, new DataGenImpl(gameDirectory.resolve("nows").resolve("generated")));
        services.register(Ui.class, UiImpl.INSTANCE);
        services.register(ConfigUi.class, new ConfigUiImpl());
        services.register(GameEvents.class, GameEventsImpl.INSTANCE);
        services.register(PlayerApi.class, new PlayerApiImpl());
    }

    public static void installBuiltInUi(NowsContext context) {
        LoaderMenu.install(context);
    }
}
