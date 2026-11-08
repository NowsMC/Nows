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

import org.apache.logging.log4j.LogManager;
import reactor.util.Logger;

/**
 * Reactor logger over the Log4j 2 API. Minecraft before 1.17 ships Log4j 2 without an SLF4J binding, and this keeps
 * Nows output in the game's own log there instead of in a separate java.util.logging stream.
 */
final class Log4j2Logger implements Logger {
    private final org.apache.logging.log4j.Logger delegate;

    Log4j2Logger(String name) {
        delegate = LogManager.getLogger(name);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public void trace(String message) {
        delegate.trace(message);
    }

    @Override
    public void trace(String format, Object... arguments) {
        delegate.trace(format, arguments);
    }

    @Override
    public void trace(String message, Throwable failure) {
        delegate.trace(message, failure);
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public void debug(String message) {
        delegate.debug(message);
    }

    @Override
    public void debug(String format, Object... arguments) {
        delegate.debug(format, arguments);
    }

    @Override
    public void debug(String message, Throwable failure) {
        delegate.debug(message, failure);
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public void info(String message) {
        delegate.info(message);
    }

    @Override
    public void info(String format, Object... arguments) {
        delegate.info(format, arguments);
    }

    @Override
    public void info(String message, Throwable failure) {
        delegate.info(message, failure);
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public void warn(String message) {
        delegate.warn(message);
    }

    @Override
    public void warn(String format, Object... arguments) {
        delegate.warn(format, arguments);
    }

    @Override
    public void warn(String message, Throwable failure) {
        delegate.warn(message, failure);
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    @Override
    public void error(String message) {
        delegate.error(message);
    }

    @Override
    public void error(String format, Object... arguments) {
        delegate.error(format, arguments);
    }

    @Override
    public void error(String message, Throwable failure) {
        delegate.error(message, failure);
    }
}
