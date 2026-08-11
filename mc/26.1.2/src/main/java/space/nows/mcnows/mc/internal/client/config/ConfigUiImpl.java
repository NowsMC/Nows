package space.nows.mcnows.mc.internal.client.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import space.nows.mcnows.mc.api.client.config.ConfigScreenBuilder;
import space.nows.mcnows.mc.api.client.config.ConfigScreenFactory;
import space.nows.mcnows.mc.api.client.config.ConfigUi;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigUiImpl implements ConfigUi {
    private final Map<String, ConfigScreenFactory> factories = new ConcurrentHashMap<>();

    @Override
    public ConfigScreenBuilder screen(Screen parent, Component title) {
        return new ConfigScreenBuilder(parent, title, spec -> new SimpleConfigScreen(spec.parent(), spec));
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
