package space.nows.mcnows.integration.logging;

import reactor.util.Logger;
import reactor.util.Loggers;

/** Reactor facade over Minecraft's existing SLF4J -> Log4j2 backend. */
public final class NowsLog {
    private static volatile boolean configured;
    private NowsLog() {}

    public static void configure() {
        if (configured) return;
        synchronized (NowsLog.class) {
            if (configured) return;
            Loggers.useSl4jLoggers();
            configured = true;
        }
    }

    public static Logger get(Class<?> type) { configure(); return Loggers.getLogger(type); }
    public static Logger get(String name) { configure(); return Loggers.getLogger(name); }
}
