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

/** Nows-owned wrapper around a shader uniform. */
public interface ShaderUniform {
    String name();

    Object nativeUniform();

    void setInt(int value);

    void setFloat(float value);

    void setVec2(float x, float y);

    void setVec3(float x, float y, float z);

    void setVec4(float x, float y, float z, float w);

    void setMatrix4(Object matrix);

    void upload();
}
