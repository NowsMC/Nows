package space.nows.mcnows.mc.api.nbt;

import java.util.LinkedHashMap;
import java.util.Map;

/** Stable compound NBT data that can be translated to a version-specific Minecraft CompoundTag. */
public final class NbtCompound {
    private final Map<String, NbtValue> values = new LinkedHashMap<>();

    public NbtCompound put(String key, NbtValue value) {
        values.put(requireKey(key), value == null ? NbtValue.end() : value);
        return this;
    }

    public NbtCompound putString(String key, String value) {
        return put(key, NbtValue.string(value));
    }

    public NbtCompound putInt(String key, int value) {
        return put(key, NbtValue.integer(value));
    }

    public NbtCompound putLong(String key, long value) {
        return put(key, NbtValue.longValue(value));
    }

    public NbtCompound putDouble(String key, double value) {
        return put(key, NbtValue.doubleValue(value));
    }

    public NbtCompound putBoolean(String key, boolean value) {
        return put(key, NbtValue.bool(value));
    }

    public Map<String, NbtValue> values() {
        return Map.copyOf(values);
    }

    private static String requireKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("NBT key must not be blank");
        }
        return key;
    }
}
