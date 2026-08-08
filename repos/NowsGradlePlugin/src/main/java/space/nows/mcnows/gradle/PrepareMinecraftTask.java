package space.nows.mcnows.gradle;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.mappingio.format.proguard.ProGuardFileReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.jar.JarFile;

public abstract class PrepareMinecraftTask extends DefaultTask {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Input public abstract Property<String> getMinecraftVersion();
    @Input public abstract Property<Boolean> getOfficialMappings();
    @OutputDirectory public abstract DirectoryProperty getOutputDirectory();
    @OutputFile public abstract RegularFileProperty getDevelopmentClientJar();

    public PrepareMinecraftTask() {
        getOfficialMappings().convention(true);
        getDevelopmentClientJar().convention(
                getOutputDirectory().file(getMinecraftVersion().map(v -> v + "/client-dev.jar")));
    }

    @TaskAction
    public void prepare() throws Exception {
        String version = getMinecraftVersion().get();
        Path dir = getOutputDirectory().get().getAsFile().toPath().resolve(version);
        Files.createDirectories(dir);

        Path raw = dir.resolve("client-runtime.jar");
        Path mappings = dir.resolve("client_mappings.txt");
        Path development = getDevelopmentClientJar().get().getAsFile().toPath();

        if (Files.isRegularFile(development) && containsNamedMinecraft(development)) {
            getLogger().lifecycle("Nows: reusing prepared Mojang-named Minecraft {} at {}.", version, development);
            return;
        }
        Path monorepoCache = getProject().getRootDir().toPath()
                .resolve(".nows")
                .resolve("minecraft")
                .resolve(version)
                .resolve("client-dev.jar");
        if (!development.equals(monorepoCache)
                && Files.isRegularFile(monorepoCache)
                && containsNamedMinecraft(monorepoCache)) {
            Files.createDirectories(development.getParent());
            Files.copy(monorepoCache, development, StandardCopyOption.REPLACE_EXISTING);
            getLogger().lifecycle("Nows: copied prepared Mojang-named Minecraft {} from {}.", version, monorepoCache);
            return;
        }

        JsonObject versionJson = resolveVersion(version);
        JsonObject downloads = versionJson.getAsJsonObject("downloads");
        DownloadSpec client = DownloadSpec.from(downloads.getAsJsonObject("client"));
        downloadVerified(client, raw);

        // Newer Minecraft releases may already ship with official names.
        if (containsNamedMinecraft(raw)) {
            Files.copy(raw, development, StandardCopyOption.REPLACE_EXISTING);
            getLogger().lifecycle("Nows: Minecraft {} is already Mojang-named; remap skipped.", version);
            return;
        }

        if (!getOfficialMappings().get()) {
            Files.copy(raw, development, StandardCopyOption.REPLACE_EXISTING);
            getLogger().warn("Nows: officialMappings=false; development JAR remains in runtime namespace.");
            return;
        }

        JsonObject mappingJson = downloads.getAsJsonObject("client_mappings");
        if (mappingJson == null) {
            throw new IOException("Minecraft " + version + " has no official client mappings");
        }
        downloadVerified(DownloadSpec.from(mappingJson), mappings);
        remapOfficial(raw, mappings, development);
        getLogger().lifecycle("Nows: remapped Minecraft {} from runtime names -> official Mojang names.", version);
    }

    private static void remapOfficial(Path input, Path proguardMappings, Path output) throws Exception {
        Files.deleteIfExists(output);
        MemoryMappingTree tree = new MemoryMappingTree();
        try (BufferedReader reader = Files.newBufferedReader(proguardMappings)) {
            // Mojang ProGuard mapping files describe named/deobfuscated -> runtime/obfuscated.
            ProGuardFileReader.read(reader, "named", "runtime", tree);
        }

        TinyRemapper remapper = TinyRemapper.newRemapper()
                .withMappings(TinyUtils.createMappingProvider(tree, "runtime", "named"))
                .renameInvalidLocals(true)
                .build();
        try (OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(output).build()) {
            outputConsumer.addNonClassFiles(input);
            remapper.readInputs(input);
            remapper.apply(outputConsumer);
        } finally {
            remapper.finish();
        }
    }

    private static boolean containsNamedMinecraft(Path jar) throws IOException {
        try (JarFile file = new JarFile(jar.toFile())) {
            return file.getEntry("net/minecraft/client/Minecraft.class") != null;
        }
    }

    private static JsonObject resolveVersion(String id) throws Exception {
        JsonObject manifest = getJson("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
        String url = manifest.getAsJsonArray("versions").asList().stream()
                .map(e -> e.getAsJsonObject())
                .filter(v -> id.equals(v.get("id").getAsString()))
                .map(v -> v.get("url").getAsString())
                .findFirst()
                .orElseThrow(() -> new IOException("Minecraft version not found: " + id));
        return getJson(url);
    }

    private static JsonObject getJson(String url) throws Exception {
        HttpResponse<String> response = HTTP.send(
                HttpRequest.newBuilder(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("HTTP " + response.statusCode() + " for " + url);
        }
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    private static void downloadVerified(DownloadSpec spec, Path target) throws Exception {
        if (isValid(target, spec)) return;

        Files.createDirectories(target.getParent());
        Path part = target.resolveSibling(target.getFileName() + ".part");
        Files.deleteIfExists(part);
        HttpResponse<Path> response = HTTP.send(
                HttpRequest.newBuilder(spec.url()).GET().build(),
                HttpResponse.BodyHandlers.ofFile(part));
        if (response.statusCode() / 100 != 2) {
            Files.deleteIfExists(part);
            throw new IOException("HTTP " + response.statusCode() + " for " + spec.url());
        }
        if (!isValid(part, spec)) {
            Files.deleteIfExists(part);
            throw new IOException("Minecraft download failed SHA-1/size validation: " + spec.url());
        }
        Files.move(part, target, StandardCopyOption.REPLACE_EXISTING);
    }

    private static boolean isValid(Path path, DownloadSpec spec) throws IOException {
        return Files.isRegularFile(path)
                && Files.size(path) == spec.size()
                && sha1(path).equalsIgnoreCase(spec.sha1());
    }

    private static String sha1(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[65536];
                int read;
                while ((read = input.read(buffer)) >= 0) digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private record DownloadSpec(URI url, String sha1, long size) {
        static DownloadSpec from(JsonObject object) {
            return new DownloadSpec(
                    URI.create(object.get("url").getAsString()),
                    object.get("sha1").getAsString(),
                    object.get("size").getAsLong());
        }
    }
}
