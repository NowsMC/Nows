package space.nows.mcnows.installer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/**
 * Internet-first Nows installer.
 *
 * <p>The release manifest decides which artifacts are downloaded and which are
 * copied out of the installer JAR. This intentionally keeps installer policy out
 * of nows-core.</p>
 */
public final class NowsInstaller {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private NowsInstaller() {}

    public static void main(String[] args) throws Exception {
        Options options = Options.parse(args);
        URI manifestUri = options.manifest != null
                ? URI.create(options.manifest)
                : URI.create("https://nows.space/releases/nows/" + options.nowsVersion + "/install.properties");

        System.out.println("[NowsInstaller] manifest: " + manifestUri);
        Properties manifest = downloadProperties(manifestUri);
        verifyManifest(manifest, options);

        List<InstalledLibrary> launcherLibraries = new ArrayList<>();
        int count = Integer.parseInt(manifest.getProperty("artifact.count", "0"));
        for (int i = 0; i < count; i++) {
            InstalledLibrary library = installArtifact(options, manifest, i);
            launcherLibraries.add(library);
            System.out.println("[NowsInstaller] installed " + library.coordinate());
        }

        installVersionJson(options, manifest, launcherLibraries);
        System.out.println("[NowsInstaller] Installed nows-" + options.nowsVersion + "-" + options.minecraftVersion);
    }

    private static InstalledLibrary installArtifact(Options options, Properties manifest, int index) throws Exception {
        String prefix = "artifact." + index + ".";
        String coordinate = required(manifest, prefix + "coordinate");
        String relativePath = required(manifest, prefix + "path").replace('\\', '/');
        String source = manifest.getProperty(prefix + "source", "internet").trim();
        String expectedSha256 = manifest.getProperty(prefix + "sha256", "").trim();
        String sourceUrl = manifest.getProperty(prefix + "url", "").trim();

        Path destination = options.minecraftDir.resolve("libraries").resolve(relativePath);
        Files.createDirectories(destination.getParent());

        if (Files.isRegularFile(destination) && !expectedSha256.isEmpty()
                && sha256(destination).equalsIgnoreCase(expectedSha256)) {
            return new InstalledLibrary(coordinate, relativePath, sourceUrl, destination);
        }

        if (source.equals("embedded")) {
            copyEmbedded(required(manifest, prefix + "resource"), destination);
        } else if (source.equals("internet")) {
            if (sourceUrl.isBlank()) {
                throw new IllegalArgumentException("Missing URL for internet artifact: " + coordinate);
            }
            download(URI.create(sourceUrl), destination);
        } else {
            throw new IllegalArgumentException("Unknown artifact source '" + source + "' for " + coordinate);
        }

        if (!expectedSha256.isEmpty()) {
            String actual = sha256(destination);
            if (!actual.equalsIgnoreCase(expectedSha256)) {
                Files.deleteIfExists(destination);
                throw new IOException("SHA-256 mismatch for " + coordinate + ": expected "
                        + expectedSha256 + ", got " + actual);
            }
        }

        return new InstalledLibrary(coordinate, relativePath, sourceUrl, destination);
    }

    private static void installVersionJson(
            Options options,
            Properties manifest,
            List<InstalledLibrary> libraries
    ) throws IOException {
        String profile = "nows-" + options.nowsVersion + "-" + options.minecraftVersion;
        Instant now = Instant.now();
        StringBuilder json = new StringBuilder();

        json.append("{\n");
        json.append("  \"id\": ").append(quote(profile)).append(",\n");
        json.append("  \"inheritsFrom\": ").append(quote(options.minecraftVersion)).append(",\n");
        json.append("  \"jar\": ").append(quote(options.minecraftVersion)).append(",\n");
        json.append("  \"mainClass\": ")
                .append(quote(manifest.getProperty("mainClass", "space.nows.mcnows.runtime.NowsLauncher")))
                .append(",\n");
        json.append("  \"type\": \"release\",\n");
        json.append("  \"time\": ").append(quote(now.toString())).append(",\n");
        json.append("  \"releaseTime\": ").append(quote(now.toString())).append(",\n");
        json.append("  \"libraries\": [\n");

        for (int i = 0; i < libraries.size(); i++) {
            InstalledLibrary lib = libraries.get(i);
            json.append("    {\"name\":").append(quote(lib.coordinate())).append(",\"downloads\":{\"artifact\":{");
            json.append("\"path\":").append(quote(lib.relativePath())).append(',');
            json.append("\"sha1\":").append(quote(sha1(lib.file()))).append(',');
            json.append("\"size\":").append(Files.size(lib.file()));
            if (!lib.sourceUrl().isBlank()) {
                json.append(",\"url\":").append(quote(lib.sourceUrl()));
            }
            json.append("}}}");
            if (i + 1 < libraries.size()) json.append(',');
            json.append('\n');
        }

        json.append("  ],\n");
        json.append("  \"arguments\": {\n");
        json.append("    \"game\": [\"--nowsMinecraftVersion\", ")
                .append(quote(options.minecraftVersion)).append("],\n");
        json.append("    \"jvm\": [");
        json.append(quote("-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector"));
        json.append(", ").append(quote("-Dnows.domain=nows.space"));
        json.append("]\n");
        json.append("  }\n");
        json.append("}\n");

        Path versionDir = options.minecraftDir.resolve("versions").resolve(profile);
        Files.createDirectories(versionDir);
        Files.writeString(versionDir.resolve(profile + ".json"), json.toString(), StandardCharsets.UTF_8);
    }

    private static Properties downloadProperties(URI uri) throws Exception {
        HttpResponse<InputStream> response = HTTP.send(
                HttpRequest.newBuilder(uri).GET().build(),
                HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("HTTP " + response.statusCode() + " for " + uri);
        }
        Properties properties = new Properties();
        try (InputStream input = response.body()) {
            properties.load(input);
        }
        return properties;
    }

    private static void download(URI uri, Path destination) throws Exception {
        Path temp = destination.resolveSibling(destination.getFileName() + ".part");
        Files.deleteIfExists(temp);
        HttpResponse<Path> response = HTTP.send(
                HttpRequest.newBuilder(uri).GET().build(),
                HttpResponse.BodyHandlers.ofFile(temp));
        if (response.statusCode() / 100 != 2) {
            Files.deleteIfExists(temp);
            throw new IOException("HTTP " + response.statusCode() + " for " + uri);
        }
        Files.move(temp, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void copyEmbedded(String resource, Path destination) throws IOException {
        try (InputStream input = NowsInstaller.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IOException("Embedded installer payload missing: " + resource);
            }
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void verifyManifest(Properties properties, Options options) {
        if (!"1".equals(properties.getProperty("format"))) {
            throw new IllegalArgumentException("Unsupported Nows install manifest format");
        }
        if (!options.nowsVersion.equals(required(properties, "nows.version"))) {
            throw new IllegalArgumentException("Manifest Nows version mismatch");
        }
        if (!options.minecraftVersion.equals(required(properties, "minecraft.version"))) {
            throw new IllegalArgumentException("Manifest Minecraft version mismatch");
        }
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing install manifest property: " + key);
        }
        return value.trim();
    }

    private static String digest(Path path, String algorithm) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            try (InputStream in = Files.newInputStream(path)) {
                byte[] buffer = new byte[65536];
                int read;
                while ((read = in.read(buffer)) >= 0) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private static String sha256(Path path) throws IOException { return digest(path, "SHA-256"); }
    private static String sha1(Path path) throws IOException { return digest(path, "SHA-1"); }

    private static String quote(String value) {
        return '"' + escape(value) + '"';
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private record InstalledLibrary(String coordinate, String relativePath, String sourceUrl, Path file) {}

    private static final class Options {
        final String nowsVersion;
        final String minecraftVersion;
        final Path minecraftDir;
        final String manifest;

        private Options(String nowsVersion, String minecraftVersion, Path minecraftDir, String manifest) {
            this.nowsVersion = nowsVersion;
            this.minecraftVersion = minecraftVersion;
            this.minecraftDir = minecraftDir;
            this.manifest = manifest;
        }

        static Options parse(String[] args) {
            String nows = "0.3.0";
            String minecraft = "26.2";
            String manifest = null;
            Path minecraftDir = Path.of(System.getProperty("user.home"), ".minecraft")
                    .toAbsolutePath().normalize();

            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--nows" -> nows = requireValue(args, ++i, "--nows");
                    case "--minecraft" -> minecraft = requireValue(args, ++i, "--minecraft");
                    case "--minecraftDir" -> minecraftDir = Path.of(requireValue(args, ++i, "--minecraftDir"))
                            .toAbsolutePath().normalize();
                    case "--manifest" -> manifest = requireValue(args, ++i, "--manifest");
                    default -> throw new IllegalArgumentException("Unknown option: " + args[i]);
                }
            }
            return new Options(nows, minecraft, minecraftDir, manifest);
        }

        private static String requireValue(String[] args, int index, String option) {
            if (index >= args.length) throw new IllegalArgumentException("Missing value for " + option);
            return args[index];
        }
    }
}
