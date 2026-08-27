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

package space.nows.mc.api;

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

/** Entry point for Minecraft-version-backed Nows APIs. */
public final class MinecraftApi {
    private MinecraftApi() {}

    public static RegistryApi registries(NowsContext context) {
        return context.service(RegistryApi.class);
    }

    public static DataPacks dataPacks(NowsContext context) {
        return context.service(DataPacks.class);
    }

    public static CommandApi commands(NowsContext context) {
        return context.service(CommandApi.class);
    }

    public static DataGen dataGen(NowsContext context) {
        return context.service(DataGen.class);
    }

    public static TextApi text(NowsContext context) {
        return context.service(TextApi.class);
    }

    public static NbtApi nbt(NowsContext context) {
        return context.service(NbtApi.class);
    }

    public static RecipeViewerApi recipeViewer(NowsContext context) {
        return context.service(RecipeViewerApi.class);
    }

    public static Ui ui(NowsContext context) {
        return context.service(Ui.class);
    }

    public static ConfigUi configUi(NowsContext context) {
        return context.service(ConfigUi.class);
    }

    public static KeybindApi keybinds(NowsContext context) {
        return context.service(KeybindApi.class);
    }

    public static GameEvents events(NowsContext context) {
        return context.service(GameEvents.class);
    }

    public static PlayerApi players(NowsContext context) {
        return context.service(PlayerApi.class);
    }

    public static PlayerApi player(NowsContext context) {
        return players(context);
    }

    public static ShaderApi shaders(NowsContext context) {
        return context.service(ShaderApi.class);
    }
}
