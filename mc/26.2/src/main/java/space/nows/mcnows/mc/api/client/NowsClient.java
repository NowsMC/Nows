package space.nows.mcnows.mc.api.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Client helpers backed directly by Minecraft 26.2 APIs. */
public final class NowsClient {
    private NowsClient() {}

    public static Minecraft minecraft() {
        return Minecraft.getInstance();
    }

    public static void show(Screen screen) {
        minecraft().setScreenAndShow(screen);
    }

    public static void execute(Runnable task) {
        minecraft().execute(task);
    }
}
