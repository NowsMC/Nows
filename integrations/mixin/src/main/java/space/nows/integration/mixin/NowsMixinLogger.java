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

package space.nows.integration.mixin;

import org.spongepowered.asm.logging.Level;
import org.spongepowered.asm.logging.LoggerAdapterAbstract;
import reactor.util.Logger;
import space.nows.integration.logging.NowsLog;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class NowsMixinLogger extends LoggerAdapterAbstract {
    private static final Map<String, NowsMixinLogger> CACHE = new ConcurrentHashMap<>();
    private final Logger delegate;

    static NowsMixinLogger get(String name) {
        return CACHE.computeIfAbsent(name, NowsMixinLogger::new);
    }

    private NowsMixinLogger(String name) {
        super(name);
        this.delegate = NowsLog.get(name);
    }

    @Override
    public String getType() {
        return "Nows Reactor Logger";
    }

    @Override
    public void catching(Level level, Throwable t) {
        log(level, "Catching " + t, t);
    }

    @Override
    public void log(Level level, String message, Object... params) {
        Throwable throwable = null;
        if (params != null && params.length > 0 && params[params.length - 1] instanceof Throwable t) {
            throwable = t;
        }
        String rendered = format(message, params);
        log(level, rendered, throwable);
    }

    @Override
    public void log(Level level, String message, Throwable t) {
        switch (level) {
            case FATAL, ERROR -> {
                if (t == null) delegate.error(message); else delegate.error(message, t);
            }
            case WARN -> {
                if (t == null) delegate.warn(message); else delegate.warn(message, t);
            }
            case DEBUG -> {
                if (t == null) delegate.debug(message); else delegate.debug(message, t);
            }
            case TRACE -> {
                if (t == null) delegate.trace(message); else delegate.trace(message, t);
            }
            default -> {
                if (t == null) delegate.info(message); else delegate.info(message, t);
            }
        }
    }

    @Override
    public <T extends Throwable> T throwing(T t) {
        log(Level.ERROR, "Throwing " + t, t);
        return t;
    }

    private static String format(String message, Object[] params) {
        if (message == null) return params == null ? "null" : Arrays.deepToString(params);
        if (params == null || params.length == 0) return message;
        StringBuilder out = new StringBuilder(message.length() + 32);
        int p = 0;
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c == '{' && i + 1 < message.length() && message.charAt(i + 1) == '}' && p < params.length) {
                Object value = params[p++];
                if (!(value instanceof Throwable && p == params.length)) out.append(value);
                i++;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
