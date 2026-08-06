package space.nows.mcnows.integration.kdl;

import dev.kdl.KdlDocument;
import dev.kdl.KdlNode;
import dev.kdl.KdlValue;
import dev.kdl.parse.KdlParser;
import space.nows.mcnows.core.mod.ModDescriptor;
import space.nows.mcnows.core.mod.ModMetadataReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarFile;

public final class KdlModMetadataReader implements ModMetadataReader {
    public static final String METADATA_PATH = "nows.mod.kdl";

    @Override
    public Optional<ModDescriptor> read(Path jarPath) throws IOException {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            var entry = jar.getJarEntry(METADATA_PATH);
            if (entry == null) return Optional.empty();
            try (InputStream input = jar.getInputStream(entry)) {
                return Optional.of(parse(KdlParser.v2().parse(input), jarPath));
            } catch (RuntimeException e) {
                throw new IOException("Invalid " + METADATA_PATH + " in " + jarPath + ": " + e.getMessage(), e);
            }
        }
    }

    static ModDescriptor parse(KdlDocument document, Path source) throws IOException {
        KdlNode mod = document.nodes().stream().filter(n -> n.name().equals("mod")).findFirst()
                .orElseThrow(() -> new IOException("Missing root 'mod' node in " + source));
        String id = requiredProperty(mod, "id", source);
        if (!id.matches("[a-z][a-z0-9_-]{1,63}")) throw new IOException("Invalid Nows mod id '" + id + "' in " + source);
        String name = property(mod, "name", id);
        String version = requiredProperty(mod, "version", source);
        String minecraft = property(mod, "minecraft", "*");

        Map<String, List<String>> declarations = new LinkedHashMap<>();
        for (KdlNode child : mod.children()) {
            if (child.arguments().isEmpty()) continue;
            Object raw = child.arguments().get(0).value();
            if (!(raw instanceof String value) || value.isBlank()) continue;
            declarations.computeIfAbsent(child.name(), ignored -> new ArrayList<>()).add(value);
        }
        if (declarations.isEmpty()) throw new IOException("Mod " + id + " has no declarations");
        return new ModDescriptor(id, name, version, minecraft, declarations);
    }

    private static String requiredProperty(KdlNode node, String key, Path source) throws IOException {
        String value = property(node, key, null);
        if (value == null || value.isBlank()) throw new IOException("Missing property '" + key + "' in " + source);
        return value;
    }

    private static String property(KdlNode node, String key, String fallback) {
        return node.<Object>getProperty(key).map(KdlValue::value).map(String::valueOf).orElse(fallback);
    }
}
