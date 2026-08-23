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

package space.nows.mcnows.minecraft;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/** Minimal bootstrap used only by this monorepo's build/example. */
public final class PrepareMinecraftCli {
    private static final Logger LOG = Logger.getLogger(PrepareMinecraftCli.class.getName());

    private PrepareMinecraftCli() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: PrepareMinecraftCli <minecraft-version> <output-jar>");
        }
        String version = args[0];
        Path output = Path.of(args[1]).toAbsolutePath().normalize();
        Path cacheDir = output.getParent();
        Files.createDirectories(cacheDir);

        MojangMetadata.VersionDownloads downloads = MojangMetadata.resolve(version);
        Path raw = MojangMetadata.downloadCached(downloads.client(), cacheDir.resolve("client-runtime.jar"));
        try (JarFile jar = new JarFile(raw.toFile())) {
            if (jar.getEntry("net/minecraft/client/Minecraft.class") == null) {
                throw new IllegalStateException(
                        "Minecraft " + version + " is not Mojang-named. "
                                + "Use NowsGradlePlugin, which owns legacy remapping support.");
            }
        }
        Files.copy(raw, output, StandardCopyOption.REPLACE_EXISTING);
        LOG.info("Nows: prepared Mojang-named Minecraft " + version + " at " + output);
    }
}
