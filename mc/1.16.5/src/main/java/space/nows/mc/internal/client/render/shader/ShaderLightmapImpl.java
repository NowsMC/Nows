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

import space.nows.mc.api.client.render.shader.ShaderLightmap;

final class ShaderLightmapImpl implements ShaderLightmap {
    @Override
    public int fullBright() {
        Class<?> lightTexture = ShaderReflection.classForName("net.minecraft.client.renderer.LightTexture")
                .orElse(null);
        return lightTexture == null ? pack(15, 15) : ShaderReflection.staticIntField(lightTexture, pack(15, 15), "FULL_BRIGHT");
    }

    @Override
    public int pack(int blockLight, int skyLight) {
        Class<?> lightTexture = ShaderReflection.classForName("net.minecraft.client.renderer.LightTexture")
                .orElse(null);
        if (lightTexture == null) {
            return (blockLight & 15) << 4 | (skyLight & 15) << 20;
        }
        return ShaderReflection.staticIntCall(lightTexture, (blockLight & 15) << 4 | (skyLight & 15) << 20,
                "pack", new Class<?>[] {int.class, int.class}, blockLight, skyLight);
    }

    @Override
    public int block(int packedLight) {
        Class<?> lightTexture = ShaderReflection.classForName("net.minecraft.client.renderer.LightTexture")
                .orElse(null);
        return lightTexture == null ? packedLight >> 4 & 15
                : ShaderReflection.staticIntCall(lightTexture, packedLight >> 4 & 15,
                        "block", new Class<?>[] {int.class}, packedLight);
    }

    @Override
    public int sky(int packedLight) {
        Class<?> lightTexture = ShaderReflection.classForName("net.minecraft.client.renderer.LightTexture")
                .orElse(null);
        return lightTexture == null ? packedLight >> 20 & 15
                : ShaderReflection.staticIntCall(lightTexture, packedLight >> 20 & 15,
                        "sky", new Class<?>[] {int.class}, packedLight);
    }

    @Override
    public void enable() {
        lightTexture().ifPresent(texture -> ShaderReflection.call(texture, "turnOnLightLayer"));
    }

    @Override
    public void disable() {
        lightTexture().ifPresent(texture -> ShaderReflection.call(texture, "turnOffLightLayer"));
    }

    private static java.util.Optional<Object> lightTexture() {
        return ShaderReflection.minecraft().flatMap(minecraft ->
                ShaderReflection.tryCallValue(minecraft, "gameRenderer")
                        .or(() -> ShaderReflection.tryFieldValue(minecraft, "gameRenderer"))
                        .flatMap(renderer -> ShaderReflection.tryCallValue(renderer, "lightTexture")));
    }
}
