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

package space.nows.minecraft;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record LaunchArguments(
        String minecraftVersion,
        String profileId,
        Path minecraftDirectory,
        Path gameDirectory,
        List<String> minecraftArguments
) {
    public static LaunchArguments parse(String[] args) {
        String minecraftVersion = null;
        String profileId = null;
        Path minecraftDirectory = null;
        Path gameDirectory = Path.of(".").toAbsolutePath().normalize();
        List<String> forwarded = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--nowsMinecraftVersion") && i + 1 < args.length) {
                minecraftVersion = args[++i];
                continue;
            }
            if (arg.equals("--version") && i + 1 < args.length) {
                String value = args[++i];
                profileId = value;
                forwarded.add(arg); forwarded.add(value); continue;
            }
            if (arg.equals("--gameDir") && i + 1 < args.length) {
                String value = args[++i];
                gameDirectory = Path.of(value).toAbsolutePath().normalize();
                forwarded.add(arg); forwarded.add(value); continue;
            }
            if (arg.equals("--assetsDir") && i + 1 < args.length) {
                String value = args[++i];
                Path assetsDirectory = Path.of(value).toAbsolutePath().normalize();
                minecraftDirectory = assetsDirectory.getParent();
                forwarded.add(arg); forwarded.add(value); continue;
            }
            forwarded.add(arg);
        }
        if (minecraftVersion == null || minecraftVersion.isBlank()) minecraftVersion = System.getProperty("nows.minecraftVersion");
        if (minecraftVersion == null || minecraftVersion.isBlank()) throw new IllegalArgumentException("Missing --nowsMinecraftVersion <version>");
        if (profileId == null || profileId.isBlank()) {
            profileId = System.getProperty("nows.profileId");
        }
        if (minecraftDirectory == null) {
            minecraftDirectory = inferMinecraftDirectory(gameDirectory);
        }
        return new LaunchArguments(minecraftVersion, profileId, minecraftDirectory, gameDirectory, List.copyOf(forwarded));
    }

    private static Path inferMinecraftDirectory(Path gameDirectory) {
        Path normalized = gameDirectory.toAbsolutePath().normalize();
        Path fileName = normalized.getFileName();
        Path profiles = normalized.getParent();
        Path nows = profiles == null ? null : profiles.getParent();
        Path minecraft = nows == null ? null : nows.getParent();
        if (fileName != null && profiles != null && nows != null && minecraft != null
                && profiles.getFileName() != null
                && nows.getFileName() != null
                && profiles.getFileName().toString().equals("profiles")
                && nows.getFileName().toString().equals("nows")) {
            return minecraft.toAbsolutePath().normalize();
        }
        return Path.of(".").toAbsolutePath().normalize();
    }
}
