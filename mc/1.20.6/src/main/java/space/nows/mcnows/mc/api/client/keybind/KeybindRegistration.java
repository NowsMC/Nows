package space.nows.mcnows.mc.api.client.keybind;

import net.minecraft.client.KeyMapping;

import java.util.Objects;

/** Registered key mapping plus its stable Nows metadata. */
public record KeybindRegistration(
        String id,
        String category,
        int defaultKeyCode,
        KeyMapping keyMapping
) {
    public KeybindRegistration {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Keybind id must not be blank");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Keybind category must not be blank");
        }
        Objects.requireNonNull(keyMapping, "keyMapping");
    }

    public boolean consumeClick() {
        return keyMapping.consumeClick();
    }

    public boolean isDown() {
        return keyMapping.isDown();
    }
}
