package space.nows.mcnows.mc.internal.client;

import net.minecraft.client.Minecraft;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.mc.api.MinecraftApi;
import space.nows.mcnows.mc.api.client.ui.Ui;

public final class LoaderMenu {
    private static final String ICON = "nows:textures/gui/mod_menu_icon.png";

    private LoaderMenu() {
    }

    public static void install(NowsContext context) {
        Ui ui = MinecraftApi.ui(context);
        ui.titleScreen().addButton(title -> title.addIconButton(
                title.width() - 24, 4, 20, 20,
                ICON,
                "Nows Mods",
                () -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    minecraft.setScreen(new ModListScreen(minecraft.screen, context));
                }
        ));
    }
}
