package space.nows.mcnows.mc.api.text;

import net.minecraft.network.chat.Component;

/** Version-backed helpers for Minecraft text components. */
public interface TextApi {
    Component literal(String text);

    Component translatable(String key, Object... args);

    Component keybind(String key);
}
