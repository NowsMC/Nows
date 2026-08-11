package space.nows.mcnows.mc.api.client.keybind;

import java.util.List;
import java.util.Optional;

/** Client keybind registry with stable category and press-callback helpers. */
public interface KeybindApi {
    String DEFAULT_CATEGORY = "key.categories.nows";

    void registerCategory(String category);

    void registerCategory(String category, int sortOrder);

    KeybindRegistration registerKeyboard(String id, int glfwKeyCode);

    KeybindRegistration registerKeyboard(String id, String category, int glfwKeyCode);

    KeybindRegistration registerKeyboard(String id, String category, int glfwKeyCode, Runnable onPress);

    void onPress(String id, Runnable listener);

    Optional<KeybindRegistration> keybind(String id);

    List<KeybindRegistration> keybinds();
}
