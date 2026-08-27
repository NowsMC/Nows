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

package space.nows.mc.internal;

import space.nows.platform.api.NowsServices;
import space.nows.platform.api.NowsContext;
import space.nows.mc.api.client.config.ConfigUi;
import space.nows.mc.api.client.keybind.KeybindApi;
import space.nows.mc.api.client.player.PlayerApi;
import space.nows.mc.api.client.render.shader.ShaderApi;
import space.nows.mc.api.client.ui.Ui;
import space.nows.mc.api.command.CommandApi;
import space.nows.mc.api.datapack.DataPacks;
import space.nows.mc.api.datagen.DataGen;
import space.nows.mc.api.event.GameEvents;
import space.nows.mc.api.nbt.NbtApi;
import space.nows.mc.api.recipe.RecipeViewerApi;
import space.nows.mc.api.registry.RegistryApi;
import space.nows.mc.api.text.TextApi;
import space.nows.mc.internal.client.config.ConfigUiImpl;
import space.nows.mc.internal.client.keybind.KeybindApiImpl;
import space.nows.mc.internal.client.player.PlayerApiImpl;
import space.nows.mc.internal.client.render.shader.ShaderApiImpl;
import space.nows.mc.internal.client.LoaderMenu;
import space.nows.mc.internal.client.UiImpl;
import space.nows.mc.internal.command.CommandApiImpl;
import space.nows.mc.internal.datapack.DataPacksImpl;
import space.nows.mc.internal.datagen.DataGenImpl;
import space.nows.mc.internal.event.GameEventsImpl;
import space.nows.mc.internal.registry.RegistryApiImpl;
import space.nows.mc.internal.nbt.NbtApiImpl;
import space.nows.mc.internal.recipe.RecipeViewerApiImpl;
import space.nows.mc.internal.text.TextApiImpl;

import java.nio.file.Path;

/** Installs Minecraft-version-backed services into the loader context. */
public final class MinecraftIntegration {
    private MinecraftIntegration() {}

    public static void install(NowsServices services, Path gameDirectory) {
        services.register(RegistryApi.class, new RegistryApiImpl());
        services.register(DataPacks.class, new DataPacksImpl(gameDirectory));
        services.register(CommandApi.class, new CommandApiImpl());
        services.register(DataGen.class, new DataGenImpl(gameDirectory.resolve("nows").resolve("generated")));
        services.register(TextApi.class, TextApiImpl.INSTANCE);
        services.register(NbtApi.class, NbtApiImpl.INSTANCE);
        services.register(RecipeViewerApi.class, new RecipeViewerApiImpl());
        services.register(Ui.class, UiImpl.INSTANCE);
        services.register(ConfigUi.class, new ConfigUiImpl());
        services.register(KeybindApi.class, new KeybindApiImpl());
        services.register(GameEvents.class, GameEventsImpl.INSTANCE);
        services.register(PlayerApi.class, new PlayerApiImpl());
        services.register(ShaderApi.class, new ShaderApiImpl("1.21.1"));
    }

    public static void installBuiltInUi(NowsContext context) {
        LoaderMenu.install(context);
    }
}
