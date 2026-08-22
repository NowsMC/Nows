package space.nows.mcnows.mc.api.nbt;

/** Stable NBT value wrapper translated by each Minecraft adapter. */
public record NbtValue(Type type, Object value) {
    public enum Type {
        END,
        BYTE,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        STRING,
        LIST,
        COMPOUND
    }

    public static NbtValue end() {
        return new NbtValue(Type.END, null);
    }

    public static NbtValue byteValue(byte value) {
        return new NbtValue(Type.BYTE, value);
    }

    public static NbtValue bool(boolean value) {
        return byteValue((byte) (value ? 1 : 0));
    }

    public static NbtValue shortValue(short value) {
        return new NbtValue(Type.SHORT, value);
    }

    public static NbtValue integer(int value) {
        return new NbtValue(Type.INT, value);
    }

    public static NbtValue longValue(long value) {
        return new NbtValue(Type.LONG, value);
    }

    public static NbtValue floatValue(float value) {
        return new NbtValue(Type.FLOAT, value);
    }

    public static NbtValue doubleValue(double value) {
        return new NbtValue(Type.DOUBLE, value);
    }

    public static NbtValue string(String value) {
        return new NbtValue(Type.STRING, value == null ? "" : value);
    }

    public static NbtValue list(NbtList value) {
        return new NbtValue(Type.LIST, value == null ? new NbtList() : value);
    }

    public static NbtValue compound(NbtCompound value) {
        return new NbtValue(Type.COMPOUND, value == null ? new NbtCompound() : value);
    }
}
