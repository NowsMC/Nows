package space.nows.mcnows.installer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
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
    private NowsInstaller() {}

    public static void main(String[] args) throws Exception {
        Options options = Options.parse(args);
        install(options, new InstallerListener() {
            @Override
            public void log(String message) {
                System.out.println(message);
            }
        });
    }

    static void install(Options options, InstallerListener listener) throws Exception {
        String manifestLocation = options.manifest != null
                ? options.manifest
                : "https://nows.space/releases/nows/" + options.nowsVersion + "/install.properties";

        listener.log("[NowsInstaller] manifest: " + manifestLocation);
        Properties manifest = loadProperties(options, manifestLocation);
        verifyManifest(manifest, options);

        List<InstalledLibrary> launcherLibraries = new ArrayList<>();
        int count = Integer.parseInt(manifest.getProperty("artifact.count", "0"));
        for (int i = 0; i < count; i++) {
            InstalledLibrary library = installArtifact(options, manifest, i);
            launcherLibraries.add(library);
            listener.log("[NowsInstaller] installed " + library.coordinate());
        }

        installVersionJson(options, manifest, launcherLibraries);
        listener.log("[NowsInstaller] Installed nows-" + options.nowsVersion + "-" + options.minecraftVersion);
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
        } else if (options.offline) {
            copyLocalArtifact(options, manifest, prefix, relativePath, destination);
        } else if (source.equals("internet")) {
            if (isBlank(sourceUrl)) {
                throw new IllegalArgumentException("Missing URL for internet artifact: " + coordinate);
            }
            download(URI.create(sourceUrl), destination);
        } else if (source.equals("local")) {
            copyLocalArtifact(options, manifest, prefix, relativePath, destination);
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

        return new InstalledLibrary(coordinate, relativePath, options.offline ? "" : sourceUrl, destination);
    }

    private static void copyLocalArtifact(
            Options options,
            Properties manifest,
            String prefix,
            String relativePath,
            Path destination
    ) throws IOException {
        Path source = localArtifactPath(options, manifest.getProperty(prefix + "file", "").trim(), relativePath);
        if (!Files.isRegularFile(source)) {
            throw new IOException("Offline artifact missing: " + source);
        }
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
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
            if (!isBlank(lib.sourceUrl())) {
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
        writeString(versionDir.resolve(profile + ".json"), json.toString());
    }

    private static Properties loadProperties(Options options, String location) throws Exception {
        if (options.offline || isLocalLocation(location)) {
            Path path = localManifestPath(options, location);
            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(path)) {
                properties.load(input);
            }
            return properties;
        }
        return downloadProperties(URI.create(location));
    }

    private static Properties downloadProperties(URI uri) throws Exception {
        HttpURLConnection connection = openConnection(uri);
        Properties properties = new Properties();
        try (InputStream input = connection.getInputStream()) {
            properties.load(input);
        } finally {
            connection.disconnect();
        }
        return properties;
    }

    private static boolean isLocalLocation(String location) {
        if (!hasUriScheme(location)) {
            return true;
        }
        String scheme = URI.create(location).getScheme();
        return scheme == null || scheme.equals("file");
    }

    private static Path localManifestPath(Options options, String location) {
        if (!hasUriScheme(location)) {
            return Paths.get(location).toAbsolutePath().normalize();
        }
        URI uri = URI.create(location);
        if (!uri.getScheme().equals("file")) {
            if (options.offline) {
                throw new IllegalArgumentException("Offline install requires a local --manifest file");
            }
            throw new IllegalArgumentException("Not a local manifest location: " + location);
        }
        return Paths.get(uri).toAbsolutePath().normalize();
    }

    private static boolean hasUriScheme(String location) {
        for (int i = 0; i < location.length(); i++) {
            char c = location.charAt(i);
            if (c == ':') {
                return i > 0;
            }
            if (!(Character.isLetterOrDigit(c) || c == '+' || c == '-' || c == '.')) {
                return false;
            }
        }
        return false;
    }

    private static Path localArtifactPath(Options options, String manifestFile, String relativePath) {
        if (!isBlank(manifestFile)) {
            Path path = Paths.get(manifestFile);
            if (path.isAbsolute()) {
                return path.normalize();
            }
            return options.artifactDir().resolve(path).normalize();
        }
        return options.artifactDir().resolve(relativePath).normalize();
    }

    private static void download(URI uri, Path destination) throws Exception {
        Path temp = destination.resolveSibling(destination.getFileName() + ".part");
        Files.deleteIfExists(temp);
        HttpURLConnection connection = openConnection(uri);
        try (InputStream input = connection.getInputStream()) {
            Files.copy(input, temp, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Files.deleteIfExists(temp);
            throw e;
        } finally {
            connection.disconnect();
        }
        Files.move(temp, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    private static HttpURLConnection openConnection(URI uri) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(30_000);
        connection.setReadTimeout(30_000);
        connection.setRequestMethod("GET");
        int status = connection.getResponseCode();
        if (status / 100 != 2) {
            connection.disconnect();
            throw new IOException("HTTP " + status + " for " + uri);
        }
        return connection;
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
        if (value == null || isBlank(value)) {
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
            return toHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private static String toHex(byte[] bytes) {
        char[] out = new char[bytes.length * 2];
        char[] hex = "0123456789abcdef".toCharArray();
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xff;
            out[i * 2] = hex[value >>> 4];
            out[i * 2 + 1] = hex[value & 0x0f];
        }
        return new String(out);
    }

    private static void writeString(Path path, String value) throws IOException {
        try (OutputStream output = Files.newOutputStream(path)) {
            output.write(value.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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

    interface InstallerListener {
        void log(String message);
    }

    private static final class InstalledLibrary {
        private final String coordinate;
        private final String relativePath;
        private final String sourceUrl;
        private final Path file;

        InstalledLibrary(String coordinate, String relativePath, String sourceUrl, Path file) {
            this.coordinate = coordinate;
            this.relativePath = relativePath;
            this.sourceUrl = sourceUrl;
            this.file = file;
        }

        String coordinate() { return coordinate; }
        String relativePath() { return relativePath; }
        String sourceUrl() { return sourceUrl; }
        Path file() { return file; }
    }

    static final class Options {
        final String nowsVersion;
        final String minecraftVersion;
        final Path minecraftDir;
        final String manifest;
        final boolean offline;
        final Path artifactDir;

        private Options(
                String nowsVersion,
                String minecraftVersion,
                Path minecraftDir,
                String manifest,
                boolean offline,
                Path artifactDir
        ) {
            this.nowsVersion = nowsVersion;
            this.minecraftVersion = minecraftVersion;
            this.minecraftDir = minecraftDir;
            this.manifest = manifest;
            this.offline = offline;
            this.artifactDir = artifactDir;
        }

        static Options parse(String[] args) {
            String nows = "0.3.0";
            String minecraft = "26.2";
            String manifest = null;
            boolean offline = false;
            Path artifactDir = null;
            Path minecraftDir = Paths.get(System.getProperty("user.home"), ".minecraft")
                    .toAbsolutePath().normalize();

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if ("--nows".equals(arg)) {
                    nows = requireValue(args, ++i, "--nows");
                } else if ("--minecraft".equals(arg)) {
                    minecraft = requireValue(args, ++i, "--minecraft");
                } else if ("--minecraftDir".equals(arg)) {
                    minecraftDir = Paths.get(requireValue(args, ++i, "--minecraftDir"))
                            .toAbsolutePath().normalize();
                } else if ("--manifest".equals(arg)) {
                    manifest = requireValue(args, ++i, "--manifest");
                } else if ("--offline".equals(arg)) {
                    offline = true;
                } else if ("--artifactDir".equals(arg)) {
                    artifactDir = Paths.get(requireValue(args, ++i, "--artifactDir"))
                            .toAbsolutePath().normalize();
                } else {
                    throw new IllegalArgumentException("Unknown option: " + arg);
                }
            }
            if (offline && manifest == null) {
                throw new IllegalArgumentException("Offline install requires --manifest pointing to a local file");
            }
            return new Options(nows, minecraft, minecraftDir, manifest, offline, artifactDir);
        }

        Path artifactDir() {
            if (artifactDir != null) {
                return artifactDir;
            }
            if (manifest != null && isLocalLocation(manifest)) {
                Path manifestPath = localManifestPath(this, manifest);
                Path parent = manifestPath.getParent();
                if (parent != null) {
                    return parent;
                }
            }
            return Paths.get("").toAbsolutePath().normalize();
        }

        private static String requireValue(String[] args, int index, String option) {
            if (index >= args.length) throw new IllegalArgumentException("Missing value for " + option);
            return args[index];
        }
    }
}
