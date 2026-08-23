/*
 * Copyright 2026 TamKungZ_ (Nows MC — https://nows.space)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.nows.integration.logging;

import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Objects;

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

    public static Logger get(Class<?> type) {
        configure();
        return Loggers.getLogger(Objects.requireNonNull(type, "type"));
    }

    public static Logger get(String name) {
        configure();
        return Loggers.getLogger(requireName(name));
    }

    public static Phase phase(Logger logger, String name) {
        return new Phase(logger, requireName(name));
    }

    private static void configureBackend(String backend) {
        String selectedBackend = backend == null || backend.isBlank() ? "slf4j" : backend.trim().toLowerCase();
        try {
            switch (selectedBackend) {
                case "console" -> Loggers.useConsoleLoggers();
                case "verbose-console" -> Loggers.useVerboseConsoleLoggers();
                case "jdk" -> Loggers.useJdkLoggers();
                case "slf4j" -> Loggers.useSl4jLoggers();
                default -> {
                    Loggers.useSl4jLoggers();
                    Loggers.getLogger(NowsLog.class).warn(
                            "Unknown Nows logging backend '{}', using slf4j", selectedBackend);
                }
            }
        } catch (RuntimeException | LinkageError failure) {
            Loggers.useJdkLoggers();
            Loggers.getLogger(NowsLog.class).warn(
                    "Nows logging backend '{}' is unavailable, using jdk fallback", selectedBackend, failure);
        }
    }

    public static final class Phase implements AutoCloseable {
        private final Logger logger;
        private final String name;
        private final long startNanos;
        private boolean closed;

        private Phase(Logger logger, String name) {
            this.logger = Objects.requireNonNull(logger, "logger");
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

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Logger name must not be blank");
        }
        return name.trim();
    }
}
