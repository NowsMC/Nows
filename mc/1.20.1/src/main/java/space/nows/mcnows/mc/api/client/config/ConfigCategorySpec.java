package space.nows.mcnows.mc.api.client.config;

import net.minecraft.network.chat.Component;
import space.nows.mcnows.mc.api.text.McText;

import java.util.List;

public record ConfigCategorySpec(Component title, List<ConfigOptionSpec> options) {
    public ConfigCategorySpec(McText title, List<ConfigOptionSpec> options) {
        this(component(title), options);
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
}
