package space.nows.mcnows.mc.api.client.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import space.nows.mcnows.mc.api.text.McText;

import java.util.Optional;

public interface ConfigUi {
    ConfigScreenBuilder screen(Screen parent, Component title);

    ConfigScreenBuilder screen(McText title);

    default ConfigScreenBuilder screen(String title) {
        return screen(McText.literal(title));
    }


    void register(String modId, ConfigScreenFactory factory);

    Optional<Screen> create(String modId, Screen parent);

    boolean has(String modId);
}
