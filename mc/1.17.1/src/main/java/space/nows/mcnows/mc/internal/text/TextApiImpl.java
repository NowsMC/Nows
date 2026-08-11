package space.nows.mcnows.mc.internal.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import space.nows.mcnows.mc.api.text.TextApi;

public enum TextApiImpl implements TextApi {
    INSTANCE;

    @Override
    public Component literal(String text) {
        return new TextComponent(text);
    }

    @Override
    public Component translatable(String key, Object... args) {
        return new TranslatableComponent(key, args);
    }

    @Override
    public Component keybind(String key) {
        return new KeybindComponent(key);
    }
}
