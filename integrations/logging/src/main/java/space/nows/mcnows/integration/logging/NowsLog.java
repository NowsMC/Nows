package space.nows.mcnows.integration.logging;

import reactor.util.Logger;
import reactor.util.Loggers;

/** Logging policy for Nows integrations over Minecraft's existing SLF4J -> Log4j2 backend. */
public final class NowsLog {
    public static final String BACKEND_PROPERTY = "nows.logging.backend";

    private static volatile boolean configured;

    private NowsLog() {}

    public static void configure() {
        if (configured) return;
        synchronized (NowsLog.class) {
            if (configured) return;
            configureBackend(System.getProperty(BACKEND_PROPERTY, "slf4j"));
            configured = true;
        }
    }

    public static Logger get(Class<?> type) { configure(); return Loggers.getLogger(type); }
    public static Logger get(String name) { configure(); return Loggers.getLogger(name); }

    public static Phase phase(Logger logger, String name) {
        return new Phase(logger, name);
    }

    private static void configureBackend(String backend) {
        try {
            switch (backend.trim().toLowerCase()) {
                case "console" -> Loggers.useConsoleLoggers();
                case "verbose-console" -> Loggers.useVerboseConsoleLoggers();
                case "jdk" -> Loggers.useJdkLoggers();
                case "slf4j" -> Loggers.useSl4jLoggers();
                default -> {
                    Loggers.useSl4jLoggers();
                    Loggers.getLogger(NowsLog.class).warn(
                            "Unknown Nows logging backend '{}', using slf4j", backend);
                }
            }
        } catch (RuntimeException | LinkageError failure) {
            Loggers.useJdkLoggers();
            Loggers.getLogger(NowsLog.class).warn(
                    "Nows logging backend '{}' is unavailable, using jdk fallback", backend, failure);
        }
    }

    public static final class Phase implements AutoCloseable {
        private final Logger logger;
        private final String name;
        private final long startNanos;
        private boolean closed;

        private Phase(Logger logger, String name) {
            this.logger = logger;
            this.name = name;
            this.startNanos = System.nanoTime();
            logger.info("{} started", name);
        }

        public void fail(Throwable failure) {
            if (!closed) {
                closed = true;
                logger.error("{} failed after {} ms", name, elapsedMillis(), failure);
            }
        }

        @Override
        public void close() {
            if (!closed) {
                closed = true;
                logger.info("{} completed in {} ms", name, elapsedMillis());
            }
        }

        private long elapsedMillis() {
            return (System.nanoTime() - startNanos) / 1_000_000L;
        }
    }
}
