package space.nows.mcnows.mc.api.nbt;

import java.util.ArrayList;
import java.util.List;

/** Stable list NBT data that can be translated to a version-specific Minecraft ListTag. */
public final class NbtList {
    private final List<NbtValue> values = new ArrayList<>();

    public NbtList add(NbtValue value) {
        values.add(value == null ? NbtValue.end() : value);
        return this;
    }

    public NbtList addString(String value) {
        return add(NbtValue.string(value));
    }

    public NbtList addInt(int value) {
        return add(NbtValue.integer(value));
    }

    public NbtList addCompound(NbtCompound value) {
        return add(NbtValue.compound(value));
    }

    public List<NbtValue> values() {
        return List.copyOf(values);
    }
}
