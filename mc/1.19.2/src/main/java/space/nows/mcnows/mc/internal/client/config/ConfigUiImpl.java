package space.nows.mcnows.mc.internal.client.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import space.nows.mcnows.mc.api.client.config.ConfigScreenBuilder;
import space.nows.mcnows.mc.api.client.config.ConfigScreenFactory;
import space.nows.mcnows.mc.api.client.config.ConfigUi;
import space.nows.mcnows.mc.api.text.McText;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigUiImpl implements ConfigUi {
    private final Map<String, ConfigScreenFactory> factories = new ConcurrentHashMap<>();

    @Override
    public ConfigScreenBuilder screen(Screen parent, Component title) {
        return new ConfigScreenBuilder(parent, title, spec -> new SimpleConfigScreen(spec.parent(), spec));
    }



    public ConfigScreenBuilder screen(McText title) {
        return screen(null, component(title));
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

    @Override
    public void register(String modId, ConfigScreenFactory factory) {
        factories.put(modId, factory);
    }

    @Override
    public Optional<Screen> create(String modId, Screen parent) {
        ConfigScreenFactory factory = factories.get(modId);
        return factory == null ? Optional.empty() : Optional.ofNullable(factory.create(parent));
    }

    @Override
    public boolean has(String modId) {
        return factories.containsKey(modId);
    }
}
