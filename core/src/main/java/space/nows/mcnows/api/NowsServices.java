package space.nows.mcnows.api;

import java.util.Map;
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
        if (services.putIfAbsent(type, type.cast(service)) != null) {
            throw new IllegalStateException("Nows service already registered: " + type.getName());
        }
    }

    public <T> Optional<T> find(Class<T> type) {
        return Optional.ofNullable(services.get(type)).map(type::cast);
    }

    public <T> T require(Class<T> type) {
        return find(type).orElseThrow(() -> new IllegalStateException(
                "Nows service is not installed: " + type.getName()));
    }
}
