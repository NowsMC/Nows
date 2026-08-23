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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class MojangMetadata {
    public static final URI VERSION_MANIFEST = URI.create("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
    private static final HttpClient HTTP = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private MojangMetadata() {}

    public static VersionDownloads resolve(String minecraftVersion) throws IOException, InterruptedException {
        JsonObject manifest = getJson(VERSION_MANIFEST);
        URI versionJson = manifest.getAsJsonArray("versions").asList().stream()
                .map(e -> e.getAsJsonObject())
                .filter(v -> minecraftVersion.equals(v.get("id").getAsString()))
                .map(v -> URI.create(v.get("url").getAsString()))
                .findFirst().orElseThrow(() -> new IOException("Minecraft version not found: " + minecraftVersion));
        JsonObject version = getJson(versionJson);
        JsonObject downloads = version.getAsJsonObject("downloads");
        return new VersionDownloads(download(downloads, "client"), optionalDownload(downloads, "client_mappings"));
    }

    private static Download download(JsonObject downloads, String key) {
        JsonObject value = downloads.getAsJsonObject(key);
        return new Download(URI.create(value.get("url").getAsString()), value.get("sha1").getAsString(), value.get("size").getAsLong());
    }

    private static Download optionalDownload(JsonObject downloads, String key) {
        return downloads.has(key) ? download(downloads, key) : null;
    }

    public static Path downloadCached(Download download, Path target) throws IOException, InterruptedException {
        Files.createDirectories(target.getParent());
        if (Files.isRegularFile(target) && Files.size(target) == download.size() && sha1(target).equals(download.sha1())) return target;
        Path temporary = target.resolveSibling(target.getFileName() + ".part");
        HttpResponse<Path> response = HTTP.send(HttpRequest.newBuilder(download.url()).GET().build(), HttpResponse.BodyHandlers.ofFile(temporary));
        if (response.statusCode() / 100 != 2) { Files.deleteIfExists(temporary); throw new IOException("HTTP " + response.statusCode() + " for " + download.url()); }
        if (Files.size(temporary) != download.size() || !sha1(temporary).equals(download.sha1())) {
            Files.deleteIfExists(temporary); throw new IOException("Downloaded Minecraft artifact failed SHA-1/size verification");
        }
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    private static JsonObject getJson(URI uri) throws IOException, InterruptedException {
        HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) throw new IOException("HTTP " + response.statusCode() + " for " + uri);
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    private static String sha1(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (var input = Files.newInputStream(path)) {
                byte[] buffer = new byte[65536]; int read;
                while ((read = input.read(buffer)) >= 0) digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) { throw new AssertionError(e); }
    }

    public record Download(URI url, String sha1, long size) {}
    public record VersionDownloads(Download client, Download clientMappings) {}
}
