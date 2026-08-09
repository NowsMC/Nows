package space.nows.mcnows.mc.api.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public interface NowsUi {
    Minecraft minecraft();

    NowsTitleScreenUi titleScreen();

    void show(Screen screen);

    default void showLater(Screen screen) {
        execute(() -> show(screen));
    }

    void close();

    default void closeLater() {
        execute(this::close);
    }

    void execute(Runnable task);

    Button button(int x, int y, int width, int height, Component message, Consumer<Button> onPress);

    Screen simpleScreen(Component title, NowsScreenInitializer initializer, NowsScreenRenderer renderer);

    void showSimpleScreen(String title, NowsScreenInitializer initializer, NowsScreenRenderer renderer);
}
