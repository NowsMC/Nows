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
import space.nows.mc.api.client.render.shader.ManagedShaderProgram;
import space.nows.mc.api.client.render.shader.ShaderApi;
import space.nows.mc.api.client.render.shader.ShaderEnvironment;
import space.nows.mc.api.client.render.shader.ShaderItemLight;
import space.nows.mc.api.client.render.shader.ShaderItemLightProvider;
import space.nows.mc.api.client.render.shader.ShaderLightmap;
import space.nows.mc.api.client.render.shader.ShaderLimits;
import space.nows.mc.api.client.render.shader.ShaderRenderStage;
import space.nows.mc.api.client.render.shader.ShaderVertexFormat;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ShaderApiImpl implements ShaderApi {
    private static final String HAND_DEPTH = "0.125";
    private static final int GL_EXTENSIONS = 0x1F03;
    private static final int GL_RENDERER = 0x1F01;
    private static final int GL_SHADING_LANGUAGE_VERSION = 0x8B8C;
    private static final int GL_VENDOR = 0x1F00;
    private static final int GL_VERSION = 0x1F02;
    private static final int GL_MAX_COLOR_ATTACHMENTS = 0x8CDF;
    private static final int GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS = 0x8B4D;
    private static final int GL_MAX_DRAW_BUFFERS = 0x8824;
    private static final int GL_MAX_SAMPLES = 0x8D57;
    private static final int GL_MAX_TEXTURE_IMAGE_UNITS = 0x8872;
    private static final int GL_MAX_UNIFORM_BUFFER_BINDINGS = 0x8A2F;
    private static final int GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT = 0x8A34;

    private final String minecraftVersion;
    private final String formattedMinecraftVersion;
    private final Map<String, ShaderItemLightProvider> itemLightProviders = new HashMap<>();
    private final ShaderLightmap lightmap = new ShaderLightmapImpl();

    public ShaderApiImpl(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
        this.formattedMinecraftVersion = formatMinecraftVersion(minecraftVersion);
    }

    @Override
    public ShaderEnvironment environment() {
        Set<String> extensions = extensions();
        return new ShaderEnvironment(
                minecraftVersion,
                formattedMinecraftVersion,
                glString(GL_VERSION),
                glString(GL_SHADING_LANGUAGE_VERSION),
                glString(GL_VENDOR),
                glString(GL_RENDERER),
                osDefine(),
                extensions,
                limits());
    }

    @Override
    public Map<String, String> standardDefines() {
        ShaderEnvironment environment = environment();
        Map<String, String> defines = new LinkedHashMap<>();
        define(defines, "MC_VERSION", formattedMinecraftVersion);
        define(defines, "MC_MIPMAP_LEVEL", Integer.toString(mipmapLevel()));
        define(defines, "MC_GL_VERSION", environment.glVersion());
        define(defines, "MC_GLSL_VERSION", environment.glslVersion());
        define(defines, environment.os(), "");
        define(defines, vendorDefine(environment.vendor()), "");
        define(defines, rendererDefine(environment.renderer()), "");
        for (String extension : environment.extensions()) {
            define(defines, "MC_" + extension, "");
        }
        define(defines, "MC_NORMAL_MAP", "");
        define(defines, "MC_SPECULAR_MAP", "");
        define(defines, "MC_RENDER_QUALITY", "1.0");
        define(defines, "MC_SHADOW_QUALITY", "1.0");
        define(defines, "MC_HAND_DEPTH", HAND_DEPTH);
        for (ShaderRenderStage stage : ShaderRenderStage.values()) {
            define(defines, "MC_RENDER_STAGE_" + stage.name(), Integer.toString(stage.ordinal()));
        }
        return Collections.unmodifiableMap(defines);
    }

    @Override
    public ShaderLightmap lightmap() {
        return lightmap;
    }

    @Override
    public ShaderItemLight itemLight(Object player, Object itemStack) {
        String itemId = itemId(itemStack).orElse("");
        ShaderItemLightProvider provider = itemLightProviders.get(itemId);
        if (provider != null) {
            return provider.light(player, itemStack);
        }
        return blockState(itemStack)
                .map(state -> ShaderReflection.intCall(state, 0, "getLightEmission"))
                .filter(emission -> emission > 0)
                .map(emission -> new ShaderItemLight(emission, 1.0F, 1.0F, 1.0F))
                .orElse(ShaderItemLight.NONE);
    }

    @Override
    public void registerItemLightProvider(String itemId, ShaderItemLightProvider provider) {
        itemLightProviders.put(itemId, provider);
    }

    @Override
    public Optional<ShaderItemLightProvider> itemLightProvider(String itemId) {
        return Optional.ofNullable(itemLightProviders.get(itemId));
    }

    @Override
    public ManagedShaderProgram loadCoreShader(String id, ShaderVertexFormat format) throws IOException {
        Object minecraft = ShaderReflection.minecraft()
                .orElseThrow(() -> new IOException("Minecraft client is not available"));
        Object resourceManager = ShaderReflection.tryCallValue(minecraft, "getResourceManager")
                .orElseThrow(() -> new IOException("Minecraft resource manager is not available"));
        Object vertexFormat = vertexFormat(format)
                .orElseThrow(() -> new IOException("Minecraft vertex format is not available: " + format));
        Class<?> shaderClass = ShaderReflection.classForName("net.minecraft.client.renderer.ShaderInstance")
                .orElseThrow(() -> new IOException("Minecraft ShaderInstance is not available in " + minecraftVersion));
        for (Constructor<?> constructor : shaderClass.getConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 3
                    && parameterTypes[0].isInstance(resourceManager)
                    && parameterTypes[1] == String.class
                    && parameterTypes[2].isInstance(vertexFormat)) {
                try {
                    return new ManagedShaderProgramImpl(id, format, constructor.newInstance(resourceManager, id, vertexFormat));
                } catch (ReflectiveOperationException exception) {
                    throw new IOException("Unable to load shader " + id, exception);
                }
            }
        }
        throw new IOException("No compatible ShaderInstance constructor found in " + minecraftVersion);
    }

    @Override
    public ManagedRenderTarget createRenderTarget(int width, int height, boolean useDepth) {
        Class<?> targetClass = ShaderReflection.classForName("com.mojang.blaze3d.pipeline.TextureTarget")
                .or(() -> ShaderReflection.classForName("com.mojang.blaze3d.pipeline.MainTarget"))
                .orElseThrow(() -> new IllegalStateException("Minecraft render target class is not available"));
        for (Constructor<?> constructor : targetClass.getConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            try {
                if (matches(parameterTypes, int.class, int.class, boolean.class, boolean.class)) {
                    return new ManagedRenderTargetImpl(constructor.newInstance(width, height, useDepth, onOsx()));
                }
                if (matches(parameterTypes, int.class, int.class, boolean.class)) {
                    return new ManagedRenderTargetImpl(constructor.newInstance(width, height, useDepth));
                }
                if (matches(parameterTypes, int.class, int.class)) {
                    return new ManagedRenderTargetImpl(constructor.newInstance(width, height));
                }
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unable to create render target", exception);
            }
        }
        throw new IllegalStateException("No compatible render target constructor found");
    }

    @Override
    public boolean isOnRenderThread() {
        return renderSystem().map(type -> ShaderReflection.staticBooleanCall(type, false, "isOnRenderThread"))
                .orElse(false);
    }

    @Override
    public void assertRenderThread() {
        renderSystem().ifPresent(type -> ShaderReflection.callStatic(type, "assertOnRenderThread"));
    }

    @Override
    public void recordRenderCall(Runnable action) {
        Optional<Class<?>> renderSystem = renderSystem();
        if (renderSystem.isPresent()
                && ShaderReflection.tryCallStatic(renderSystem.get(), "recordRenderCall",
                        new Class<?>[] {Runnable.class}, action)) {
            return;
        }
        action.run();
    }

    private static Optional<Class<?>> renderSystem() {
        return ShaderReflection.classForName("com.mojang.blaze3d.systems.RenderSystem");
    }

    private static Optional<Object> vertexFormat(ShaderVertexFormat format) {
        return ShaderReflection.classForName("com.mojang.blaze3d.vertex.DefaultVertexFormat")
                .flatMap(type -> ShaderReflection.tryStaticFieldValue(type, vertexFormatField(format)));
    }

    private static String vertexFormatField(ShaderVertexFormat format) {
        return switch (format) {
            case POSITION -> "POSITION";
            case POSITION_COLOR -> "POSITION_COLOR";
            case POSITION_TEX -> "POSITION_TEX";
            case POSITION_COLOR_TEX -> "POSITION_COLOR_TEX";
            case POSITION_TEX_COLOR -> "POSITION_TEX_COLOR";
            case POSITION_COLOR_TEX_LIGHTMAP -> "POSITION_COLOR_TEX_LIGHTMAP";
            case POSITION_TEX_COLOR_NORMAL -> "POSITION_TEX_COLOR_NORMAL";
            case POSITION_COLOR_NORMAL -> "POSITION_COLOR_NORMAL";
            case POSITION_COLOR_LIGHTMAP -> "POSITION_COLOR_LIGHTMAP";
            case BLOCK -> "BLOCK";
            case NEW_ENTITY -> "NEW_ENTITY";
            case PARTICLE -> "PARTICLE";
        };
    }

    private static Optional<Object> blockState(Object itemStack) {
        return item(itemStack)
                .filter(item -> ShaderReflection.classForName("net.minecraft.world.item.BlockItem")
                        .map(type -> type.isInstance(item))
                        .orElse(false))
                .flatMap(item -> ShaderReflection.tryCallValue(item, "getBlock"))
                .flatMap(block -> ShaderReflection.tryCallValue(block, "defaultBlockState"));
    }

    private static Optional<String> itemId(Object itemStack) {
        return item(itemStack).flatMap(item -> registryKey(
                "net.minecraft.core.registries.BuiltInRegistries",
                "ITEM",
                item).or(() -> registryKey(
                "net.minecraft.core.Registry",
                "ITEM",
                item)));
    }

    private static Optional<Object> item(Object itemStack) {
        return ShaderReflection.tryCallValue(itemStack, "getItem");
    }

    private static Optional<String> registryKey(String registryClass, String field, Object value) {
        return ShaderReflection.classForName(registryClass)
                .flatMap(type -> ShaderReflection.tryStaticFieldValue(type, field))
                .flatMap(registry -> ShaderReflection.tryCallValue(registry, "getKey", new Class<?>[] {value.getClass()}, value)
                        .or(() -> ShaderReflection.callFirstCompatible(registry, "getKey", value)))
                .map(Object::toString);
    }

    private static boolean matches(Class<?>[] actual, Class<?>... expected) {
        if (actual.length != expected.length) {
            return false;
        }
        for (int index = 0; index < actual.length; index++) {
            if (actual[index] != expected[index]) {
                return false;
            }
        }
        return true;
    }

    private static boolean onOsx() {
        return ShaderReflection.classForName("net.minecraft.client.Minecraft")
                .map(type -> ShaderReflection.staticBooleanField(type, false, "ON_OSX", "ON_OS"))
                .orElse(false);
    }

    private static void define(Map<String, String> defines, String key, String value) {
        if (!key.isBlank()) {
            defines.put(key, value);
        }
    }

    private static String glString(int key) {
        Optional<Class<?>> glState = ShaderReflection.classForName("com.mojang.blaze3d.platform.GlStateManager")
                .or(() -> ShaderReflection.classForName("com.mojang.blaze3d.opengl.GlStateManager"));
        return glState.flatMap(type -> ShaderReflection.tryCallStaticValue(type, "_getString", new Class<?>[] {int.class}, key))
                .map(Object::toString)
                .orElse("");
    }

    private static int glInt(int key) {
        Optional<Class<?>> glState = ShaderReflection.classForName("com.mojang.blaze3d.platform.GlStateManager")
                .or(() -> ShaderReflection.classForName("com.mojang.blaze3d.opengl.GlStateManager"));
        return glState.map(type -> ShaderReflection.staticIntCall(type, 0, "_getInteger", new Class<?>[] {int.class}, key))
                .orElse(0);
    }

    private static Set<String> extensions() {
        Set<String> extensions = new LinkedHashSet<>();
        String extensionString = glString(GL_EXTENSIONS);
        if (!extensionString.isBlank()) {
            Collections.addAll(extensions, extensionString.split(" "));
        }
        return Collections.unmodifiableSet(extensions);
    }

    private static ShaderLimits limits() {
        return new ShaderLimits(
                glInt(GL_MAX_TEXTURE_IMAGE_UNITS),
                glInt(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS),
                glInt(GL_MAX_DRAW_BUFFERS),
                glInt(GL_MAX_COLOR_ATTACHMENTS),
                glInt(GL_MAX_SAMPLES),
                glInt(GL_MAX_UNIFORM_BUFFER_BINDINGS),
                glInt(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT));
    }

    private static int mipmapLevel() {
        return ShaderReflection.minecraft()
                .flatMap(minecraft -> ShaderReflection.tryFieldValue(minecraft, "options")
                        .or(() -> ShaderReflection.tryFieldValue(minecraft, "options")))
                .flatMap(options -> ShaderReflection.tryCallValue(options, "mipmapLevels"))
                .flatMap(level -> ShaderReflection.tryCallValue(level, "get"))
                .filter(Integer.class::isInstance)
                .map(Integer.class::cast)
                .orElse(0);
    }

    private static String osDefine() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return "MC_OS_WINDOWS";
        }
        if (os.contains("mac")) {
            return "MC_OS_MAC";
        }
        if (os.contains("linux")) {
            return "MC_OS_LINUX";
        }
        return "MC_OS_UNKNOWN";
    }

    private static String vendorDefine(String vendor) {
        String normalized = vendor.toLowerCase(Locale.ROOT);
        if (normalized.contains("nvidia")) {
            return "MC_GL_VENDOR_NVIDIA";
        }
        if (normalized.contains("amd") || normalized.contains("ati")) {
            return "MC_GL_VENDOR_AMD";
        }
        if (normalized.contains("intel")) {
            return "MC_GL_VENDOR_INTEL";
        }
        return "MC_GL_VENDOR_UNKNOWN";
    }

    private static String rendererDefine(String renderer) {
        String normalized = renderer.toLowerCase(Locale.ROOT);
        if (normalized.contains("mesa")) {
            return "MC_GL_RENDERER_MESA";
        }
        if (normalized.contains("radeon")) {
            return "MC_GL_RENDERER_RADEON";
        }
        if (normalized.contains("geforce") || normalized.contains("nvidia")) {
            return "MC_GL_RENDERER_GEFORCE";
        }
        if (normalized.contains("intel")) {
            return "MC_GL_RENDERER_INTEL";
        }
        return "MC_GL_RENDERER_UNKNOWN";
    }

    private static String formatMinecraftVersion(String version) {
        String[] parts = version.split("\\.");
        int major = numberPart(parts, 0);
        int minor = numberPart(parts, 1);
        int patch = numberPart(parts, 2);
        if (major >= 26) {
            return String.format(Locale.ROOT, "%02d%02d%02d", major, minor, patch);
        }
        return String.format(Locale.ROOT, "%d%02d%02d", major, minor, patch);
    }

    private static int numberPart(String[] parts, int index) {
        if (index >= parts.length) {
            return 0;
        }
        String digits = parts[index].replaceAll("\\D.*", "");
        return digits.isBlank() ? 0 : Integer.parseInt(digits);
    }
}
