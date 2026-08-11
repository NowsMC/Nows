package space.nows.mcnows.mc.internal.client;

import space.nows.mcnows.mc.api.client.ui.TitleScreenUi;
import space.nows.mcnows.mc.api.client.ui.Ui;

public final class UiImpl implements Ui {
    public static final UiImpl INSTANCE = new UiImpl();

    private final TitleScreenUiImpl titleScreen = new TitleScreenUiImpl();

    private UiImpl() {}

    @Override
    public TitleScreenUi titleScreen() {
        return titleScreen;
    }

    public TitleScreenUiImpl titleScreenImpl() {
        return titleScreen;
    }
}
