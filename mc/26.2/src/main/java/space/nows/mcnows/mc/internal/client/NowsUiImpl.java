package space.nows.mcnows.mc.internal.client;

import space.nows.mcnows.mc.api.client.ui.NowsTitleScreenUi;
import space.nows.mcnows.mc.api.client.ui.NowsUi;

public final class NowsUiImpl implements NowsUi {
    public static final NowsUiImpl INSTANCE = new NowsUiImpl();

    private final NowsTitleScreenUiImpl titleScreen = new NowsTitleScreenUiImpl();

    private NowsUiImpl() {}

    @Override
    public NowsTitleScreenUi titleScreen() {
        return titleScreen;
    }

    public NowsTitleScreenUiImpl titleScreenImpl() {
        return titleScreen;
    }
}
