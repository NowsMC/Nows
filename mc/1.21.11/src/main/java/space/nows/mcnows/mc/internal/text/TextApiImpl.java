package space.nows.mcnows.mc.internal.text;

import net.minecraft.network.chat.Component;
import space.nows.mcnows.mc.api.text.TextApi;

public enum TextApiImpl implements TextApi {
    INSTANCE;

    @Override
    public Component literal(String text) {
        return Component.literal(text);
    }

    @Override
    public Component translatable(String key, Object... args) {
        return Component.translatable(key, args);
    }

    @Override
    public Component keybind(String key) {
        return Component.keybind(key);
    }
}
