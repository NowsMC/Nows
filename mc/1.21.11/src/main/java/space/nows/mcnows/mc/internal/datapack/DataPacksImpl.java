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

package space.nows.mcnows.mc.internal.datapack;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import space.nows.mcnows.mc.api.datapack.DataPacks;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class DataPacksImpl implements DataPacks {
    private final Path gameDirectory;
    private final Map<PackType, List<RepositorySource>> sources = new EnumMap<>(PackType.class);

    public DataPacksImpl(Path gameDirectory) {
        this.gameDirectory = gameDirectory;
    }

    @Override
    public Path gameDirectory() {
        return gameDirectory;
    }

    @Override
    public Path nowsPackDirectory() {
        return gameDirectory.resolve("nows").resolve("packs");
    }

    @Override
    public Path modPackDirectory(String modId) {
        return nowsPackDirectory().resolve(modId);
    }

    @Override
    public void registerSource(PackType type, RepositorySource source) {
        sources.computeIfAbsent(type, ignored -> new ArrayList<>()).add(source);
    }

    @Override
    public List<RepositorySource> sources(PackType type) {
        return List.copyOf(sources.getOrDefault(type, List.of()));
    }

    @Override
    public PackRepository repository(PackType type) {
        return new PackRepository(sources(type).toArray(RepositorySource[]::new));
    }
}
