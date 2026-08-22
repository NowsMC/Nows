package space.nows.mcnows.mc.api.client.keybind;

import java.util.Objects;

/** Registered key mapping plus its stable Nows metadata. */
public record KeybindRegistration(
        String id,
        String category,
        int defaultKeyCode,
        Object nativeKeyMapping
) {
    public KeybindRegistration {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Keybind id must not be blank");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Keybind category must not be blank");
        }
        Objects.requireNonNull(nativeKeyMapping, "nativeKeyMapping");
    }

    public Object keyMapping() {
        return nativeKeyMapping;
    }

    public boolean consumeClick() {
        return invokeBoolean("consumeClick");
    }

    public boolean isDown() {
        return invokeBoolean("isDown");
    }

    private boolean invokeBoolean(String methodName) {
        try {
            return (Boolean) nativeKeyMapping.getClass().getMethod(methodName).invoke(nativeKeyMapping);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to call native key mapping method: " + methodName, exception);
        }
    }
}
