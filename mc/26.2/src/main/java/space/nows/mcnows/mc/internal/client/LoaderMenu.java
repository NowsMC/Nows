package space.nows.mcnows.mc.internal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.mc.api.MinecraftApi;
import space.nows.mcnows.mc.api.client.ui.Ui;

public final class LoaderMenu {
    private static final String ICON = "nows:textures/gui/mod_menu_icon.png";
    private static final ThreadLocal<Screen> TITLE_PARENT = new ThreadLocal<>();

    private LoaderMenu() {
    }

    public static void withTitleParent(Screen parent, Runnable action) {
        Screen previous = TITLE_PARENT.get();
        TITLE_PARENT.set(parent);
        try {
            action.run();
        } finally {
            if (previous == null) {
                TITLE_PARENT.remove();
            } else {
                TITLE_PARENT.set(previous);
            }
        }
    }

    public static void install(NowsContext context) {
        Ui ui = MinecraftApi.ui(context);
        ui.titleScreen().addButton(title -> title.addIconButton(
                title.width() - 24, 4, 20, 20,
                ICON,
                "Nows Mods",
                () -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    minecraft.setScreenAndShow(new ModListScreen(TITLE_PARENT.get(), context));
                }
        ));
    }
}
