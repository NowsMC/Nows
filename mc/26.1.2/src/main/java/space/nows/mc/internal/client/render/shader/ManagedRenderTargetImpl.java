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

import space.nows.mc.api.client.render.shader.ManagedRenderTarget;

final class ManagedRenderTargetImpl implements ManagedRenderTarget {
    private final Object target;

    ManagedRenderTargetImpl(Object target) {
        this.target = target;
    }

    @Override
    public int width() {
        return ShaderReflection.intField(target, "width", "viewWidth");
    }

    @Override
    public int height() {
        return ShaderReflection.intField(target, "height", "viewHeight");
    }

    @Override
    public int colorTextureId() {
        return ShaderReflection.intCall(target, 0, "getColorTextureId", "getColorTexture");
    }

    @Override
    public int depthTextureId() {
        return ShaderReflection.intCall(target, 0, "getDepthTextureId", "getDepthTexture");
    }

    @Override
    public Object nativeTarget() {
        return target;
    }

    @Override
    public void bindWrite(boolean setViewport) {
        ShaderReflection.call(target, "bindWrite", new Class<?>[] {boolean.class}, setViewport);
    }

    @Override
    public void bindRead() {
        ShaderReflection.call(target, "bindRead");
    }

    @Override
    public void unbindWrite() {
        ShaderReflection.call(target, "unbindWrite");
    }

    @Override
    public void resize(int width, int height) {
        if (!ShaderReflection.tryCall(target, "resize", new Class<?>[] {int.class, int.class, boolean.class}, width, height, false)) {
            ShaderReflection.call(target, "resize", new Class<?>[] {int.class, int.class}, width, height);
        }
    }

    @Override
    public void clear(boolean onOsx) {
        ShaderReflection.call(target, "clear", new Class<?>[] {boolean.class}, onOsx);
    }

    @Override
    public void setClearColor(float red, float green, float blue, float alpha) {
        ShaderReflection.call(target, "setClearColor",
                new Class<?>[] {float.class, float.class, float.class, float.class}, red, green, blue, alpha);
    }

    @Override
    public void close() {
        ShaderReflection.call(target, "destroyBuffers");
    }
}
