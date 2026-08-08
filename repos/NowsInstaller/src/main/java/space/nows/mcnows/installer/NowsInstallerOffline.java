package space.nows.mcnows.installer;

public final class NowsInstallerOffline {
    private NowsInstallerOffline() {}

    public static void main(String[] args) throws Exception {
        NowsInstaller.Options options = NowsInstaller.Options.parse(args).withEmbeddedOfflinePayload();
        NowsInstaller.install(options, new NowsInstaller.InstallerListener() {
            @Override
            public void log(String message) {
                System.out.println(message);
            }
        });
    }
}
