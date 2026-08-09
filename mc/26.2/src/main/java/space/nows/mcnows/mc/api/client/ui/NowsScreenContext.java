package space.nows.mcnows.mc.api.client.ui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;

public final class NowsScreenContext {
    private final Screen screen;
    private final Function<AbstractWidget, AbstractWidget> addWidget;

    public NowsScreenContext(Screen screen, Function<AbstractWidget, AbstractWidget> addWidget) {
        this.screen = screen;
        this.addWidget = addWidget;
    }

    public Screen screen() {
        return screen;
    }

    public int width() {
        return screen.width;
    }

    public int height() {
        return screen.height;
    }

    public int centerX(int width) {
        return (width() - width) / 2;
    }

    public int centerY(int height) {
        return (height() - height) / 2;
    }

    public Button button(int x, int y, int width, int height, Component message, Consumer<Button> onPress) {
        return Button.builder(message, button -> onPress.accept(button))
                .bounds(x, y, width, height)
                .build();
    }

    public Button addButton(int x, int y, int width, int height, Component message, Consumer<Button> onPress) {
        return addWidget(button(x, y, width, height, message, onPress));
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractWidget> T addWidget(T widget) {
        return (T) addWidget.apply(widget);
    }
}
