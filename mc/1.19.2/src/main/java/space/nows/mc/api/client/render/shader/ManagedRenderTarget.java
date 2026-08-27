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

/** Nows-owned wrapper around a version-specific framebuffer/render target. */
public interface ManagedRenderTarget extends AutoCloseable {
    int width();

    int height();

    int colorTextureId();

    int depthTextureId();

    Object nativeTarget();

    void bindWrite(boolean setViewport);

    void bindRead();

    void unbindWrite();

    void resize(int width, int height);

    void clear(boolean onOsx);

    void setClearColor(float red, float green, float blue, float alpha);

    @Override
    void close();
}
