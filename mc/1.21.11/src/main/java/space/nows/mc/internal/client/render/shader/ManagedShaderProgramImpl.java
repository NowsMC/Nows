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

import space.nows.mc.api.client.render.shader.ManagedShaderProgram;
import space.nows.mc.api.client.render.shader.ShaderUniform;
import space.nows.mc.api.client.render.shader.ShaderVertexFormat;

final class ManagedShaderProgramImpl implements ManagedShaderProgram {
    private final String id;
    private final ShaderVertexFormat format;
    private final Object shader;

    ManagedShaderProgramImpl(String id, ShaderVertexFormat format, Object shader) {
        this.id = id;
        this.format = format;
        this.shader = shader;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public ShaderVertexFormat format() {
        return format;
    }

    @Override
    public Object nativeProgram() {
        return shader;
    }

    @Override
    public void apply() {
        ShaderReflection.call(shader, "apply");
    }

    @Override
    public void clear() {
        ShaderReflection.call(shader, "clear");
    }

    @Override
    public void sampler(String name, Object sampler) {
        ShaderReflection.call(shader, "setSampler", new Class<?>[] {String.class, Object.class}, name, sampler);
    }

    @Override
    public ShaderUniform uniform(String name) {
        Object uniform = ShaderReflection.tryCallValue(shader, "safeGetUniform", new Class<?>[] {String.class}, name)
                .or(() -> ShaderReflection.tryCallValue(shader, "getUniform", new Class<?>[] {String.class}, name))
                .orElse(null);
        return new ShaderUniformImpl(name, uniform);
    }

    @Override
    public void close() {
        ShaderReflection.call(shader, "close");
    }
}
