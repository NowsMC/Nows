package space.nows.integration.logging;

import org.junit.jupiter.api.Test;
import reactor.util.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Log4j2LoggerTest {
    @Test
    void reportsItsNameAndTheLog4jLevelFlags() {
        Logger logger = new Log4j2Logger("space.nows.test");

        assertEquals("space.nows.test", logger.getName());
        assertTrue(logger.isErrorEnabled(), "Log4j 2 enables error by default");
        assertFalse(logger.isTraceEnabled(), "Log4j 2 keeps trace off by default");
    }

    @Test
    void acceptsEveryMessageShapeAtEveryLevel() {
        Logger logger = new Log4j2Logger("space.nows.test");
        Throwable failure = new IllegalStateException("expected by the test");

        logger.trace("plain");
        logger.trace("with {} and {}", "a", 1);
        logger.trace("with throwable", failure);
        logger.debug("plain");
        logger.debug("with {} and {}", "a", 1);
        logger.debug("with throwable", failure);
        logger.info("plain");
        logger.info("with {} and {}", "a", 1);
        logger.info("with throwable", failure);
        logger.warn("plain");
        logger.warn("with {} and {}", "a", 1);
        logger.warn("with throwable", failure);
        logger.error("plain");
        logger.error("with {} and {}", "a", 1);
        logger.error("with throwable", failure);
    }
}
