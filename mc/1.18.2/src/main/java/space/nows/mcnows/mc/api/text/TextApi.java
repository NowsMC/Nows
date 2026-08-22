package space.nows.mcnows.mc.api.text;

import net.minecraft.network.chat.Component;

/** Version-backed helpers for Minecraft text components. */
public interface TextApi {
    default Component component(McText text) {
        if (text == null) {
            return literal("");
        }
        return switch (text.type()) {
            case LITERAL -> literal(text.value());
            case TRANSLATABLE -> translatable(text.value(), text.args());
            case KEYBIND -> keybind(text.value());
        };
    }

    Component literal(String text);

    Component translatable(String key, Object... args);

    Component keybind(String key);
}
