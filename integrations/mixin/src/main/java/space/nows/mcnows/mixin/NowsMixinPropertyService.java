package space.nows.mcnows.mixin;

import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Nows-owned Mixin blackboard. Keys are namespaced by their resolved string value. */
public final class NowsMixinPropertyService implements IGlobalPropertyService {
    private final Map<IPropertyKey, Object> values = new ConcurrentHashMap<>();

    @Override
    public IPropertyKey resolveKey(String name) {
        return new Key(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(IPropertyKey key) {
        return (T) values.get(key);
    }

    @Override
    public void setProperty(IPropertyKey key, Object value) {
        if (value == null) values.remove(key);
        else values.put(key, value);
    }

    @Override
    public <T> T getProperty(IPropertyKey key, T defaultValue) {
        T value = getProperty(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public String getPropertyString(IPropertyKey key, String defaultValue) {
        Object value = values.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    private record Key(String name) implements IPropertyKey {
        private Key {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Mixin property key cannot be blank");
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
