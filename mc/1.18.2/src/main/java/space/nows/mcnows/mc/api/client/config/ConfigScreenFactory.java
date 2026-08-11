package space.nows.mcnows.mc.api.client.config;

import net.minecraft.client.gui.screens.Screen;

@FunctionalInterface
public interface ConfigScreenFactory {
    Screen create(Screen parent);
}
