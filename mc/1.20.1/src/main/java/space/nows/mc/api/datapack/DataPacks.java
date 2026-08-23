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

package space.nows.mc.api.datapack;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import space.nows.mc.api.datapack.PackTarget;

import java.nio.file.Path;
import java.util.List;

/** Version-backed datapack and resource-pack source registry. */
public interface DataPacks {
    Path gameDirectory();

    Path nowsPackDirectory();

    Path modPackDirectory(String modId);

    default Path modAssetsDirectory(String modId) {
        return modPackDirectory(modId).resolve("assets").resolve(modId);
    }

    default Path modDataDirectory(String modId) {
        return modPackDirectory(modId).resolve("data").resolve(modId);
    }


    void registerSource(PackType type, RepositorySource source);

    default void registerSource(PackTarget target, Object source) {
        registerSource(packType(target), (RepositorySource) source);
    }


    List<RepositorySource> sources(PackType type);

    default List<Object> sources(PackTarget target) {
        return List.copyOf(sources(packType(target)));
    }


    PackRepository repository(PackType type);

    default Object repository(PackTarget target) {
        return repository(packType(target));
    }

    private static PackType packType(PackTarget target) {
        return target == PackTarget.CLIENT_RESOURCES ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA;
    }

}
