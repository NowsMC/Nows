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

package space.nows.mc.internal.resources;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.flag.FeatureFlagSet;
import space.nows.platform.core.mod.ModContainer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.ZipFile;

public final class ModPackSource implements RepositorySource {
    private static final int RESOURCE_PACK_FORMAT_1_19_4 = 13;
    private static final Pack.Info INFO = new Pack.Info(
            Component.literal("Nows mod resources"),
            RESOURCE_PACK_FORMAT_1_19_4,
            FeatureFlagSet.of());
    private static final PackMetadataSection METADATA = new PackMetadataSection(
            Component.literal("Nows mod resources"),
            RESOURCE_PACK_FORMAT_1_19_4);

    private final List<ModContainer> mods;
    private final Path loaderResources;

    private ModPackSource(List<ModContainer> mods, Path loaderResources) {
        this.mods = mods;
        this.loaderResources = loaderResources;
    }

    public static Set<RepositorySource> appendClientSource(
            Set<RepositorySource> sources,
            RepositorySource[] originalSources,
            List<ModContainer> mods) {
        int resourceMods = countResourceMods(mods);
        Path loaderResources = ownJar();
        boolean hasLoaderResources = loaderResources != null && hasResources(loaderResources);
        if ((resourceMods == 0 && !hasLoaderResources) || !isClientResourceRepository(originalSources)) {
            return sources;
        }
        if (sources.stream().anyMatch(ModPackSource.class::isInstance)) {
            return sources;
        }
        LinkedHashSet<RepositorySource> updated = new LinkedHashSet<>(sources);
        updated.add(new ModPackSource(List.copyOf(mods), loaderResources));
        System.out.println("[Nows] Added resource pack source for loader resources and " + resourceMods + " mod(s)");
        return Collections.unmodifiableSet(updated);
    }

    @Override
    public void loadPacks(Consumer<Pack> output) {
        if (loaderResources != null && hasResources(loaderResources)) {
            output.accept(pack("nows/loader", Component.literal("Nows loader resources"), loaderResources));
        }
        for (ModContainer mod : mods) {
            if (!hasResources(mod.path())) {
                continue;
            }
            String id = "nows/" + mod.descriptor().id();
            System.out.println("[Nows] Loading mod resource pack: " + mod.descriptor().id());
            output.accept(pack(id, Component.literal(mod.descriptor().name()), mod.path()));
        }
    }

    private static Pack pack(String id, Component title, Path jar) {
        return Pack.create(
                id,
                title,
                true,
                packId -> open(packId, jar),
                INFO,
                PackType.CLIENT_RESOURCES,
                Pack.Position.TOP,
                true,
                PackSource.BUILT_IN);
    }

    static PackMetadataSection metadata() {
        return METADATA;
    }

    private static int countResourceMods(List<ModContainer> mods) {
        int count = 0;
        for (ModContainer mod : mods) {
            if (hasResources(mod.path())) {
                count++;
            }
        }
        return count;
    }

    private static boolean isClientResourceRepository(RepositorySource[] sources) {
        for (RepositorySource source : sources) {
            if (source.getClass().getName().equals("net.minecraft.client.resources.ClientPackSource")) {
                return true;
            }
        }
        return false;
    }

    private static PackResources open(String id, Path jar) {
        try {
            return new ModPackResources(id, jar);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open Nows mod resource pack " + jar, e);
        }
    }

    private static Path ownJar() {
        try {
            return Path.of(ModPackSource.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .toAbsolutePath()
                    .normalize();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean hasResources(Path jar) {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            return zip.stream().anyMatch(entry -> {
                String name = entry.getName();
                return !entry.isDirectory() && (name.startsWith("assets/") || name.startsWith("data/"));
            });
        } catch (IOException e) {
            return false;
        }
    }
}
