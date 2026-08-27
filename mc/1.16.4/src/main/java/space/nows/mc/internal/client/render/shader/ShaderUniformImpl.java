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

package space.nows.mc.internal.client.render.shader;

import space.nows.mc.api.client.render.shader.ShaderUniform;

final class ShaderUniformImpl implements ShaderUniform {
    private final String name;
    private final Object uniform;

    ShaderUniformImpl(String name, Object uniform) {
        this.name = name;
        this.uniform = uniform;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Object nativeUniform() {
        return uniform;
    }

    @Override
    public void setInt(int value) {
        invoke("set", new Class<?>[] {int.class}, value);
    }

    @Override
    public void setFloat(float value) {
        invoke("set", new Class<?>[] {float.class}, value);
    }

    @Override
    public void setVec2(float x, float y) {
        invoke("set", new Class<?>[] {float.class, float.class}, x, y);
    }

    @Override
    public void setVec3(float x, float y, float z) {
        invoke("set", new Class<?>[] {float.class, float.class, float.class}, x, y, z);
    }

    @Override
    public void setVec4(float x, float y, float z, float w) {
        invoke("set", new Class<?>[] {float.class, float.class, float.class, float.class}, x, y, z, w);
    }

    @Override
    public void setMatrix4(Object matrix) {
        ShaderReflection.callAssignable(uniform, "set", matrix);
    }

    @Override
    public void upload() {
        ShaderReflection.tryCall(uniform, "upload");
    }

    private void invoke(String methodName, Class<?>[] parameterTypes, Object... arguments) {
        if (uniform == null) {
            return;
        }
        ShaderReflection.call(uniform, methodName, parameterTypes, arguments);
    }
}
