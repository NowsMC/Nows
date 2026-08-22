package space.nows.mcnows.mc.api.client.config;

import net.minecraft.network.chat.Component;
import space.nows.mcnows.mc.api.text.McText;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

public final class ConfigOptionSpec {
    public enum Type {
        BOOLEAN,
        INTEGER
    }

    private final Type type;
    private final Component label;
    private final Component tooltip;
    private boolean booleanValue;
    private int intValue;
    private final boolean defaultBooleanValue;
    private final int defaultIntValue;
    private final int min;
    private final int max;
    private final Consumer<Boolean> booleanSaveConsumer;
    private final IntConsumer intSaveConsumer;

    private ConfigOptionSpec(
            Type type,
            Component label,
            Component tooltip,
            boolean booleanValue,
            int intValue,
            boolean defaultBooleanValue,
            int defaultIntValue,
            int min,
            int max,
            Consumer<Boolean> booleanSaveConsumer,
            IntConsumer intSaveConsumer
    ) {
        this.type = type;
        this.label = label;
        this.tooltip = tooltip;
        this.booleanValue = booleanValue;
        this.intValue = intValue;
        this.defaultBooleanValue = defaultBooleanValue;
        this.defaultIntValue = defaultIntValue;
        this.min = min;
        this.max = max;
        this.booleanSaveConsumer = booleanSaveConsumer;
        this.intSaveConsumer = intSaveConsumer;
    }

    public static ConfigOptionSpec bool(
            Component label,
            boolean value,
            boolean defaultValue,
            Component tooltip,
            Consumer<Boolean> saveConsumer
    ) {
        return new ConfigOptionSpec(Type.BOOLEAN, label, tooltip, value, 0, defaultValue, 0, 0, 0,
                saveConsumer, null);
    }

    public static ConfigOptionSpec bool(
            McText label,
            boolean value,
            boolean defaultValue,
            McText tooltip,
            Consumer<Boolean> saveConsumer
    ) {
        return bool(component(label), value, defaultValue, component(tooltip), saveConsumer);
    }

    public static ConfigOptionSpec bool(
            String label,
            boolean value,
            boolean defaultValue,
            String tooltip,
            Consumer<Boolean> saveConsumer
    ) {
        return bool(McText.literal(label), value, defaultValue, McText.literal(tooltip), saveConsumer);
    }

    public static ConfigOptionSpec integer(
            Component label,
            int value,
            int defaultValue,
            int min,
            int max,
            Component tooltip,
            IntConsumer saveConsumer
    ) {
        return new ConfigOptionSpec(Type.INTEGER, label, tooltip, false, value, false, defaultValue, min, max,
                null, saveConsumer);
    }

    public static ConfigOptionSpec integer(
            McText label,
            int value,
            int defaultValue,
            int min,
            int max,
            McText tooltip,
            IntConsumer saveConsumer
    ) {
        return integer(component(label), value, defaultValue, min, max, component(tooltip), saveConsumer);
    }

    public static ConfigOptionSpec integer(
            String label,
            int value,
            int defaultValue,
            int min,
            int max,
            String tooltip,
            IntConsumer saveConsumer
    ) {
        return integer(McText.literal(label), value, defaultValue, min, max, McText.literal(tooltip), saveConsumer);
    }

    private static Component component(McText text) {
        if (text == null) {
            return Component.literal("");
        }
        return switch (text.type()) {
            case LITERAL -> Component.literal(text.value());
            case TRANSLATABLE -> Component.translatable(text.value(), text.args());
            case KEYBIND -> Component.keybind(text.value());
        };
    }

    public Type type() {

        return type;
    }

    public Component label() {
        return label;
    }

    public Component tooltip() {
        return tooltip;
    }

    public boolean booleanValue() {
        return booleanValue;
    }

    public int intValue() {
        return intValue;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    public void toggle() {
        booleanValue = !booleanValue;
    }

    public void add(int delta) {
        intValue = Math.max(min, Math.min(max, intValue + delta));
    }

    public void reset() {
        if (type == Type.BOOLEAN) {
            booleanValue = defaultBooleanValue;
        } else {
            intValue = defaultIntValue;
        }
    }

    public void save() {
        if (type == Type.BOOLEAN && booleanSaveConsumer != null) {
            booleanSaveConsumer.accept(booleanValue);
        } else if (type == Type.INTEGER && intSaveConsumer != null) {
            intSaveConsumer.accept(intValue);
        }
    }
}
