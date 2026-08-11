package space.nows.mcnows.mc.api.client.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public interface ConfigUi {
    ConfigScreenBuilder screen(Screen parent, Component title);

    void register(String modId, ConfigScreenFactory factory);

    Optional<Screen> create(String modId, Screen parent);

    boolean has(String modId);
}
