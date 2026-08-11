package space.nows.mcnows.mc.api.client.ui;

@FunctionalInterface
public interface SimpleScreenSink {
    void showSimpleScreen(String title, ScreenInitializer initializer, ScreenRenderer renderer);
}
