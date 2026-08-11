package space.nows.mcnows.mc.internal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.mc.api.NowsMinecraft;
import space.nows.mcnows.mc.api.client.ui.NowsUi;

public final class NowsLoaderMenu {
    private static final String ICON = "nows:textures/gui/mod_menu_icon.png";

    private NowsLoaderMenu() {
    }

    public static void install(NowsContext context) {
        NowsUi ui = NowsMinecraft.ui(context);
        ui.titleScreen().addButton(title -> title.addIconButton(
                title.width() - 24, 4, 20, 20,
                ICON,
                "Nows Mods",
                () -> Minecraft.getInstance().setScreen(new NowsModListScreen(new TitleScreen(), context))
        ));
    }
}
