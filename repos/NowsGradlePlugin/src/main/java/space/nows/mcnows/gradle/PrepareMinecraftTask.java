package space.nows.mcnows.gradle;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

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
        Files.createDirectories(output.getParent());
        NowsMappings mappings = NowsMappings.read(proguardMappings);
        NowsRemapper remapper = new NowsRemapper(mappings);
        Path part = output.resolveSibling(output.getFileName() + ".part");
        Files.deleteIfExists(part);
        Set<String> written = new HashSet<>();
        try (JarFile inputJar = new JarFile(input.toFile());
             OutputStream fileOutput = Files.newOutputStream(part);
             JarOutputStream outputJar = new JarOutputStream(fileOutput)) {
            var entries = inputJar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                if (isSignatureFile(name)) {
                    continue;
                }
                if (name.endsWith(".class")) {
                    byte[] remapped = remapClass(inputJar, entry, remapper);
                    String originalClassName = name.substring(0, name.length() - ".class".length());
                    writeEntry(outputJar, written, remapper.mapType(originalClassName) + ".class", remapped);
                    continue;
                }
                try (InputStream entryInput = inputJar.getInputStream(entry)) {
                    writeEntry(outputJar, written, name, entryInput.readAllBytes());
                }
            }
        } catch (Exception failure) {
            Files.deleteIfExists(part);
            throw failure;
        }
        Files.move(part, output, StandardCopyOption.REPLACE_EXISTING);
    }

    private static byte[] remapClass(JarFile jar, JarEntry entry, Remapper remapper) throws IOException {
        try (InputStream input = jar.getInputStream(entry)) {
            ClassReader reader = new ClassReader(input.readAllBytes());
            ClassWriter writer = new ClassWriter(0);
            reader.accept(new ClassRemapper(writer, remapper), 0);
            return writer.toByteArray();
        }
    }

    private static void writeEntry(JarOutputStream jar, Set<String> written, String name, byte[] bytes) throws IOException {
        if (!written.add(name)) {
            return;
        }
        JarEntry outputEntry = new JarEntry(name);
        outputEntry.setTime(0L);
        jar.putNextEntry(outputEntry);
        jar.write(bytes);
        jar.closeEntry();
    }

    private static boolean isSignatureFile(String name) {
        String upper = name.toUpperCase(java.util.Locale.ROOT);
        return upper.startsWith("META-INF/") && (upper.endsWith(".SF") || upper.endsWith(".RSA") || upper.endsWith(".DSA"));
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

    private record MemberKey(String owner, String name, String descriptor) {}

    private static final class NowsMappings {
        private final Map<String, String> classes = new HashMap<>();
        private final Map<String, String> namedToRuntimeClasses = new HashMap<>();
        private final Map<MemberKey, String> fields = new HashMap<>();
        private final Map<MemberKey, String> methods = new HashMap<>();

        private static NowsMappings read(Path proguardMappings) throws IOException {
            NowsMappings mappings = new NowsMappings();
            java.util.List<String> lines = Files.readAllLines(proguardMappings);
            for (String line : lines) {
                if (line.isBlank() || line.startsWith("#") || Character.isWhitespace(line.charAt(0))) {
                    continue;
                }
                String trimmed = line.trim();
                if (!trimmed.endsWith(":")) {
                    continue;
                }
                String body = trimmed.substring(0, trimmed.length() - 1);
                int arrow = body.indexOf(" -> ");
                if (arrow < 0) {
                    continue;
                }
                String namedOwner = javaNameToInternal(body.substring(0, arrow).trim());
                String runtimeOwner = javaNameToInternal(body.substring(arrow + 4).trim());
                mappings.classes.put(runtimeOwner, namedOwner);
                mappings.namedToRuntimeClasses.put(namedOwner, runtimeOwner);
            }

            String namedOwner = null;
            String runtimeOwner = null;
            for (String line : lines) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                if (!Character.isWhitespace(line.charAt(0))) {
                    String trimmed = line.trim();
                    if (!trimmed.endsWith(":")) {
                        continue;
                    }
                    String body = trimmed.substring(0, trimmed.length() - 1);
                    int arrow = body.indexOf(" -> ");
                    if (arrow < 0) {
                        continue;
                    }
                    namedOwner = javaNameToInternal(body.substring(0, arrow).trim());
                    runtimeOwner = javaNameToInternal(body.substring(arrow + 4).trim());
                    continue;
                }
                if (namedOwner == null || runtimeOwner == null) {
                    continue;
                }
                mappings.readMember(runtimeOwner, line.trim());
            }
            return mappings;
        }

        private void readMember(String runtimeOwner, String line) {
            int arrow = line.indexOf(" -> ");
            if (arrow < 0) {
                return;
            }
            String left = stripLineNumbers(line.substring(0, arrow).trim());
            String runtimeName = line.substring(arrow + 4).trim();
            int argsStart = left.indexOf('(');
            if (argsStart >= 0) {
                int argsEnd = left.indexOf(')', argsStart);
                int nameStart = left.lastIndexOf(' ', argsStart);
                if (argsEnd < 0 || nameStart < 0) {
                    return;
                }
                String namedName = left.substring(nameStart + 1, argsStart).trim();
                if (namedName.startsWith("<")) {
                    return;
                }
                String returnType = left.substring(0, nameStart).trim();
                String arguments = left.substring(argsStart + 1, argsEnd).trim();
                String runtimeDescriptor = methodDescriptor(returnType, arguments, false);
                methods.put(new MemberKey(runtimeOwner, runtimeName, runtimeDescriptor), namedName);
                return;
            }

            int nameStart = left.lastIndexOf(' ');
            if (nameStart < 0) {
                return;
            }
            String type = left.substring(0, nameStart).trim();
            String namedName = left.substring(nameStart + 1).trim();
            String runtimeDescriptor = typeDescriptor(type, false);
            fields.put(new MemberKey(runtimeOwner, runtimeName, runtimeDescriptor), namedName);
        }

        private String mapClass(String runtimeInternalName) {
            return classes.getOrDefault(runtimeInternalName, runtimeInternalName);
        }

        private String mapField(String owner, String name, String descriptor) {
            return fields.getOrDefault(new MemberKey(owner, name, descriptor), name);
        }

        private String mapMethod(String owner, String name, String descriptor) {
            return methods.getOrDefault(new MemberKey(owner, name, descriptor), name);
        }

        private static String stripLineNumbers(String value) {
            value = value.replaceFirst("^\\d+:\\d+:", "");
            value = value.replaceFirst(":\\d+:\\d+$", "");
            return value;
        }

        private String methodDescriptor(String returnType, String arguments, boolean named) {
            StringBuilder descriptor = new StringBuilder("(");
            if (!arguments.isBlank()) {
                for (String argument : splitArguments(arguments)) {
                    descriptor.append(typeDescriptor(argument, named));
                }
            }
            descriptor.append(')').append(typeDescriptor(returnType, named));
            return descriptor.toString();
        }

        private static java.util.List<String> splitArguments(String arguments) {
            java.util.ArrayList<String> result = new java.util.ArrayList<>();
            int genericDepth = 0;
            int start = 0;
            for (int i = 0; i < arguments.length(); i++) {
                char c = arguments.charAt(i);
                if (c == '<') genericDepth++;
                else if (c == '>') genericDepth--;
                else if (c == ',' && genericDepth == 0) {
                    result.add(arguments.substring(start, i).trim());
                    start = i + 1;
                }
            }
            result.add(arguments.substring(start).trim());
            return result;
        }

        private String typeDescriptor(String javaType, boolean named) {
            String type = stripGenerics(javaType.trim()).replace("...", "[]");
            int dimensions = 0;
            while (type.endsWith("[]")) {
                dimensions++;
                type = type.substring(0, type.length() - 2).trim();
            }

            String descriptor = switch (type) {
                case "void" -> "V";
                case "boolean" -> "Z";
                case "byte" -> "B";
                case "char" -> "C";
                case "short" -> "S";
                case "int" -> "I";
                case "float" -> "F";
                case "long" -> "J";
                case "double" -> "D";
                default -> {
                    String internalName = javaNameToInternal(type);
                    yield "L" + (named ? internalName : namedToRuntimeClasses.getOrDefault(internalName, internalName)) + ";";
                }
            };

            if (dimensions == 0) {
                return descriptor;
            }
            return "[".repeat(dimensions) + descriptor;
        }

        private static String stripGenerics(String type) {
            StringBuilder result = new StringBuilder();
            int depth = 0;
            for (int i = 0; i < type.length(); i++) {
                char c = type.charAt(i);
                if (c == '<') {
                    depth++;
                } else if (c == '>') {
                    depth--;
                } else if (depth == 0) {
                    result.append(c);
                }
            }
            String stripped = result.toString().trim();
            if (stripped.startsWith("? extends ")) {
                return stripped.substring("? extends ".length()).trim();
            }
            if (stripped.startsWith("? super ")) {
                return stripped.substring("? super ".length()).trim();
            }
            return stripped;
        }

        private static String javaNameToInternal(String name) {
            return name.replace('.', '/');
        }
    }

    private static final class NowsRemapper extends Remapper {
        private final NowsMappings mappings;

        private NowsRemapper(NowsMappings mappings) {
            this.mappings = mappings;
        }

        @Override
        public String map(String internalName) {
            return mappings.mapClass(internalName);
        }

        @Override
        public String mapFieldName(String owner, String name, String descriptor) {
            return mappings.mapField(owner, name, descriptor);
        }

        @Override
        public String mapMethodName(String owner, String name, String descriptor) {
            return mappings.mapMethod(owner, name, descriptor);
        }
    }
}
