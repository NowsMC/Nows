package space.nows.mcnows.mc.internal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import space.nows.mcnows.mc.api.client.ui.NowsScreenInitializer;
import space.nows.mcnows.mc.api.client.ui.NowsScreenRenderer;
import space.nows.mcnows.mc.api.client.ui.NowsTitleScreenUi;
import space.nows.mcnows.mc.api.client.ui.NowsUi;

import java.util.function.Consumer;

public final class NowsUiImpl implements NowsUi {
    public static final NowsUiImpl INSTANCE = new NowsUiImpl();

    private final NowsTitleScreenUiImpl titleScreen = new NowsTitleScreenUiImpl();

    private NowsUiImpl() {}

    @Override
    public Minecraft minecraft() {
        return Minecraft.getInstance();
    }

    @Override
    public NowsTitleScreenUi titleScreen() {
        return titleScreen;
    }

    public NowsTitleScreenUiImpl titleScreenImpl() {
        return titleScreen;
    }

    @Override
    public void show(Screen screen) {
        minecraft().setScreenAndShow(screen);
    }

    @Override
    public void close() {
        show(null);
    }

    @Override
    public void execute(Runnable task) {
        minecraft().execute(task);
    }

    @Override
    public Button button(int x, int y, int width, int height, Component message, Consumer<Button> onPress) {
        return Button.builder(message, button -> onPress.accept(button))
                .bounds(x, y, width, height)
                .build();
    }

    @Override
    public Screen simpleScreen(Component title, NowsScreenInitializer initializer, NowsScreenRenderer renderer) {
        return new NowsSimpleScreen(title, initializer, renderer);
    }

    @Override
    public void showSimpleScreen(String title, NowsScreenInitializer initializer, NowsScreenRenderer renderer) {
        show(simpleScreen(Component.literal(title), initializer, renderer));
    }
}
