package space.nows.mcnows.mc.api.client.ui;

@FunctionalInterface
public interface NowsSimpleScreenSink {
    void showSimpleScreen(String title, NowsScreenInitializer initializer, NowsScreenRenderer renderer);
}
