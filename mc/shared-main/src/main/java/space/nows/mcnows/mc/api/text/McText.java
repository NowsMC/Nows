package space.nows.mcnows.mc.api.text;

import java.util.Arrays;

/** Stable text description translated to Minecraft Component by each adapter. */
public final class McText {
    public enum Type {
        LITERAL,
        TRANSLATABLE,
        KEYBIND
    }

    private final Type type;
    private final String value;
    private final Object[] args;

    private McText(Type type, String value, Object[] args) {
        this.type = type;
        this.value = value == null ? "" : value;
        this.args = args == null ? new Object[0] : Arrays.copyOf(args, args.length);
    }

    public static McText literal(String text) {
        return new McText(Type.LITERAL, text, null);
    }

    public static McText translatable(String key, Object... args) {
        return new McText(Type.TRANSLATABLE, key, args);
    }

    public static McText keybind(String key) {
        return new McText(Type.KEYBIND, key, null);
    }

    public Type type() {
        return type;
    }

    public String value() {
        return value;
    }

    public Object[] args() {
        return Arrays.copyOf(args, args.length);
    }
}
