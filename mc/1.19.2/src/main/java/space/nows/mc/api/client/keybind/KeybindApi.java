/*
 * Copyright 2026 TamKungZ_ (Nows MC — https://nows.space)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.nows.mc.api.client.keybind;

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
