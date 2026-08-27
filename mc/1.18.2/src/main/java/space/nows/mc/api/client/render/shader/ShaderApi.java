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

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/** Stable 1.20.1 rendering/shader entrypoint owned by Nows. */
public interface ShaderApi {
    ShaderEnvironment environment();

    Map<String, String> standardDefines();

    ShaderLightmap lightmap();

    ShaderItemLight itemLight(Object player, Object itemStack);

    void registerItemLightProvider(String itemId, ShaderItemLightProvider provider);

    Optional<ShaderItemLightProvider> itemLightProvider(String itemId);

    ManagedShaderProgram loadCoreShader(String id, ShaderVertexFormat format) throws IOException;

    ManagedRenderTarget createRenderTarget(int width, int height, boolean useDepth);

    boolean isOnRenderThread();

    void assertRenderThread();

    void recordRenderCall(Runnable action);
}
