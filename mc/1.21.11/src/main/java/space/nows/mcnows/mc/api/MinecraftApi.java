package space.nows.mcnows.mc.api;

import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.mc.api.client.config.ConfigUi;
import space.nows.mcnows.mc.api.client.player.PlayerApi;
import space.nows.mcnows.mc.api.client.ui.Ui;
import space.nows.mcnows.mc.api.command.CommandApi;
import space.nows.mcnows.mc.api.datapack.DataPacks;
import space.nows.mcnows.mc.api.datagen.DataGen;
import space.nows.mcnows.mc.api.event.GameEvents;
import space.nows.mcnows.mc.api.registry.RegistryApi;
import space.nows.mcnows.mc.api.text.TextApi;

/** Entry point for Minecraft-version-backed Nows APIs. */
public final class MinecraftApi {
    private MinecraftApi() {}

    public static RegistryApi registries(NowsContext context) {
        return context.service(RegistryApi.class);
    }

    public static DataPacks dataPacks(NowsContext context) {
        return context.service(DataPacks.class);
    }

    public static CommandApi commands(NowsContext context) {
        return context.service(CommandApi.class);
    }

    public static DataGen dataGen(NowsContext context) {
        return context.service(DataGen.class);
    }

    public static TextApi text(NowsContext context) {
        return context.service(TextApi.class);
    }

    public static Ui ui(NowsContext context) {
        return context.service(Ui.class);
    }

    public static ConfigUi configUi(NowsContext context) {
        return context.service(ConfigUi.class);
    }

    public static GameEvents events(NowsContext context) {
        return context.service(GameEvents.class);
    }

    public static PlayerApi players(NowsContext context) {
        return context.service(PlayerApi.class);
    }

    public static PlayerApi player(NowsContext context) {
        return players(context);
    }
}
