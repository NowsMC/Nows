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

package space.nows.mc.api.client.render.shader;

/** Stable item-held light value for shader integrations. */
public record ShaderItemLight(int emission, float red, float green, float blue) {
    public static final ShaderItemLight NONE = new ShaderItemLight(0, 1.0F, 1.0F, 1.0F);

    public ShaderItemLight {
        if (emission < 0 || emission > 15) {
            throw new IllegalArgumentException("Item light emission must be between 0 and 15");
        }
    }
}
