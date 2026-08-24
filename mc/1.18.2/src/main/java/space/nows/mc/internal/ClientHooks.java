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

import net.minecraft.server.packs.repository.RepositorySource;
import space.nows.platform.core.mod.ModContainer;
import space.nows.mc.internal.resources.ModPackSource;

import java.util.List;
import java.util.Set;

public final class ClientHooks {
    private static volatile String nowsVersion = "development";
    private static volatile String minecraftVersion = "unknown";
    private static volatile int modCount;
    private static volatile List<ModContainer> mods = List.of();

    private ClientHooks() {
    }

    public static void configure(String nows, String minecraft, int mods) {
        nowsVersion = nows;
        minecraftVersion = minecraft;
        modCount = mods;
    }

    public static void configure(String nows, String minecraft, int mods, List<ModContainer> modContainers) {
        configure(nows, minecraft, mods);
        ClientHooks.mods = List.copyOf(modContainers);
    }

    public static Set<RepositorySource> withModResourcePackSource(Set<RepositorySource> sources, RepositorySource[] originalSources) {
        return ModPackSource.appendClientSource(sources, originalSources, mods);
    }

    public static String loaderLine() {
        return "Nows Loader " + nowsVersion;
    }

    public static String minecraftLine() {
        return minecraftVersion;
    }

    public static String modLine() {
        return modCount + " Nows mod" + (modCount == 1 ? "" : "s") + " loaded";
    }
}
