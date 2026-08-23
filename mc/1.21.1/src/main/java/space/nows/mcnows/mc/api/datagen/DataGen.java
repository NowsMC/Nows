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

package space.nows.mcnows.mc.api.datagen;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;

/** Small generated-data writer for recipes, tags and other JSON assets. */
public interface DataGen {
    Moshi moshi();

    Path outputDirectory();

    Path path(String relativePath);

    void writeText(String relativePath, String text) throws IOException;

    void writeJson(String relativePath, String json) throws IOException;

    @SuppressWarnings({"unchecked", "rawtypes"})
    default void writeJson(String relativePath, Object value) throws IOException {
        Class<?> type = value == null ? Object.class : value.getClass();
        writeJson(relativePath, (JsonAdapter<Object>) moshi().adapter((Class) type), value);
    }

    default <T> void writeJson(String relativePath, Class<T> type, T value) throws IOException {
        writeJson(relativePath, jsonAdapter(type), value);
    }

    default <T> void writeJson(String relativePath, Type type, T value) throws IOException {
        writeJson(relativePath, jsonAdapter(type), value);
    }

    default <T> void writeJson(String relativePath, JsonAdapter<T> adapter, T value) throws IOException {
        writeJson(relativePath, adapter.indent("  ").toJson(value));
    }

    default <T> JsonAdapter<T> jsonAdapter(Class<T> type) {
        return moshi().adapter(type);
    }

    default <T> JsonAdapter<T> jsonAdapter(Type type) {
        return moshi().adapter(type);
    }

    default String recipePath(String id) {
        return "data/" + namespace(id) + "/recipe/" + pathPart(id) + ".json";
    }

    default String itemTagPath(String id) {
        return "data/" + namespace(id) + "/tags/item/" + pathPart(id) + ".json";
    }

    default String blockTagPath(String id) {
        return "data/" + namespace(id) + "/tags/block/" + pathPart(id) + ".json";
    }

    private static String namespace(String id) {
        int split = id.indexOf(':');
        return split >= 0 ? id.substring(0, split) : "minecraft";
    }

    private static String pathPart(String id) {
        int split = id.indexOf(':');
        return split >= 0 ? id.substring(split + 1) : id;
    }
}
