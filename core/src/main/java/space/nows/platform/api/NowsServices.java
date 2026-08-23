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

package space.nows.platform.api;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small typed service registry that keeps optional integrations out of nows-core.
 * GEB, Mixin helpers and future systems are exposed through this registry instead
 * of becoming hard dependencies of the stable API.
 */
public final class NowsServices {
    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    public <T> void register(Class<T> type, T service) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(service, "service");
        if (services.putIfAbsent(type, type.cast(service)) != null) {
            throw new IllegalStateException("Nows service already registered: " + type.getName());
        }
    }

    public <T> Optional<T> find(Class<T> type) {
        Objects.requireNonNull(type, "type");
        return Optional.ofNullable(services.get(type)).map(type::cast);
    }

    public <T> T require(Class<T> type) {
        Objects.requireNonNull(type, "type");
        return find(type).orElseThrow(() -> new IllegalStateException(
                "Nows service is not installed: " + type.getName()));
    }
}
