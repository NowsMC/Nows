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

package space.nows.mcnows.mc.internal.datagen;

import com.squareup.moshi.Moshi;
import space.nows.mcnows.mc.api.datagen.DataGen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DataGenImpl implements DataGen {
    private final Moshi moshi = new Moshi.Builder().build();
    private final Path outputDirectory;

    public DataGenImpl(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public Moshi moshi() {
        return moshi;
    }

    @Override
    public Path outputDirectory() {
        return outputDirectory;
    }

    @Override
    public Path path(String relativePath) {
        return outputDirectory.resolve(relativePath).normalize();
    }

    @Override
    public void writeText(String relativePath, String text) throws IOException {
        Path file = path(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, text, StandardCharsets.UTF_8);
    }

    @Override
    public void writeJson(String relativePath, String json) throws IOException {
        writeText(relativePath, json.endsWith("\n") ? json : json + "\n");
    }
}
