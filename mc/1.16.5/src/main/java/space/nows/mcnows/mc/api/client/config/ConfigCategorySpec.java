package space.nows.mcnows.mc.api.client.config;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import space.nows.mcnows.mc.api.text.McText;

import java.util.List;

public record ConfigCategorySpec(Component title, List<ConfigOptionSpec> options) {
    public ConfigCategorySpec(McText title, List<ConfigOptionSpec> options) {
        this(component(title), options);
    }

    private static Component component(McText text) {
        if (text == null) {
            return new TextComponent("");
        }
        return switch (text.type()) {
            case LITERAL -> new TextComponent(text.value());
            case TRANSLATABLE -> new TranslatableComponent(text.value(), text.args());
            case KEYBIND -> new KeybindComponent(text.value());
        };
    }
}
