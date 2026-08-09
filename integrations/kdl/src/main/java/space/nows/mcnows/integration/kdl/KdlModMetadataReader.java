package space.nows.mcnows.integration.kdl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarFile;

import dev.kdl.KdlDocument;
import dev.kdl.KdlNode;
import dev.kdl.KdlValue;
import dev.kdl.parse.KdlParseException;
import dev.kdl.parse.KdlParser;
import space.nows.mcnows.core.mod.ModDescriptor;
import space.nows.mcnows.core.mod.ModDependency;
import space.nows.mcnows.core.mod.ModMetadataReader;

public final class KdlModMetadataReader implements ModMetadataReader {
    public static final String METADATA_PATH = "nows.mod.kdl";

    @Override
    public Optional<ModDescriptor> read(Path jarPath) throws IOException {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            var entry = jar.getJarEntry(METADATA_PATH);
            if (entry == null) return Optional.empty();
            try (InputStream input = jar.getInputStream(entry)) {
                return Optional.of(parse(KdlParser.v2().parse(input), jarPath));
            } catch (KdlParseException | RuntimeException e) {
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
        String description = property(mod, "description", "");
        String icon = property(mod, "icon", "");

        List<String> authors = new ArrayList<>(propertyValues(mod, "author"));
        authors.addAll(propertyValues(mod, "authors"));
        List<String> contributors = new ArrayList<>(propertyValues(mod, "contributor"));
        contributors.addAll(propertyValues(mod, "contributors"));
        List<String> licenses = new ArrayList<>(propertyValues(mod, "license"));
        licenses.addAll(propertyValues(mod, "licenses"));
        Map<String, String> contacts = new LinkedHashMap<>();
        Map<String, String> properties = rootProperties(mod);
        List<ModDependency> dependencies = new ArrayList<>();
        Map<String, List<String>> declarations = new LinkedHashMap<>();
        for (KdlNode child : mod.children()) {
            switch (child.name()) {
                case "description" -> description = firstArgument(child, description);
                case "author", "authors" -> authors.addAll(stringArguments(child));
                case "contributor", "contributors" -> contributors.addAll(stringArguments(child));
                case "license", "licenses" -> licenses.addAll(stringArguments(child));
                case "icon" -> icon = firstArgument(child, icon);
                case "contact" -> readContactNode(child, contacts);
                case "homepage", "website", "sources", "source", "issues", "wiki", "discord", "email" ->
                        contacts.put(contactKey(child.name()), firstArgument(child, ""));
                case "property" -> readPropertyNode(child, properties);
                case "depends", "dependency", "requires", "recommends", "suggests", "breaks", "conflicts" ->
                        dependencies.add(readDependency(child));
                default -> readDeclaration(child, declarations, properties);
            }
        }
        if (declarations.isEmpty()) throw new IOException("Mod " + id + " has no declarations");
        return new ModDescriptor(id, name, version, minecraft, description, authors, contributors, licenses, icon,
                contacts, properties, dependencies, declarations);
    }

    private static String requiredProperty(KdlNode node, String key, Path source) throws IOException {
        String value = property(node, key, null);
        if (value == null || value.isBlank()) throw new IOException("Missing property '" + key + "' in " + source);
        return value;
    }

    private static String property(KdlNode node, String key, String fallback) {
        return node.<Object>getProperty(key).map(KdlValue::value).map(String::valueOf).orElse(fallback);
    }

    private static List<String> propertyValues(KdlNode node, String key) {
        return node.properties().getValues(key).stream()
                .map(KdlValue::value)
                .map(String::valueOf)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private static Map<String, String> rootProperties(KdlNode mod) {
        Map<String, String> result = new LinkedHashMap<>();
        for (var entry : mod.properties()) {
            String key = entry.getKey();
            if (isCoreProperty(key)) continue;
            List<KdlValue<?>> values = entry.getValue();
            if (!values.isEmpty()) {
                result.put(key, valueToString(values.get(values.size() - 1)));
            }
        }
        return result;
    }

    private static boolean isCoreProperty(String key) {
        return switch (key) {
            case "id", "name", "version", "minecraft", "description", "icon",
                    "author", "authors", "contributor", "contributors", "license", "licenses" -> true;
            default -> false;
        };
    }

    private static void readContactNode(KdlNode node, Map<String, String> contacts) {
        List<String> arguments = stringArguments(node);
        if (arguments.size() >= 2) {
            contacts.put(arguments.get(0), arguments.get(1));
        }
        for (var entry : node.properties()) {
            List<KdlValue<?>> values = entry.getValue();
            if (!values.isEmpty()) {
                contacts.put(contactKey(entry.getKey()), valueToString(values.get(values.size() - 1)));
            }
        }
    }

    private static String contactKey(String key) {
        return key.equals("source") ? "sources" : key;
    }

    private static void readPropertyNode(KdlNode node, Map<String, String> properties) {
        List<String> arguments = stringArguments(node);
        if (arguments.size() >= 2) {
            properties.put(arguments.get(0), arguments.get(1));
        }
        for (var entry : node.properties()) {
            List<KdlValue<?>> values = entry.getValue();
            if (!values.isEmpty()) {
                properties.put(entry.getKey(), valueToString(values.get(values.size() - 1)));
            }
        }
    }

    private static ModDependency readDependency(KdlNode node) {
        String id = property(node, "id", "");
        List<String> arguments = stringArguments(node);
        if (id.isBlank() && !arguments.isEmpty()) {
            id = arguments.get(0);
        }
        String version = property(node, "version", property(node, "range", arguments.size() >= 2 ? arguments.get(1) : "*"));
        boolean optional = booleanProperty(node, "optional", node.name().equals("recommends") || node.name().equals("suggests"));
        String reason = property(node, "reason", "");
        return new ModDependency(node.name(), id, version, optional, reason);
    }

    private static boolean booleanProperty(KdlNode node, String key, boolean fallback) {
        return node.<Object>getProperty(key).map(KdlValue::value)
                .map(value -> value instanceof Boolean bool ? bool : Boolean.parseBoolean(String.valueOf(value)))
                .orElse(fallback);
    }

    private static void readDeclaration(
            KdlNode node,
            Map<String, List<String>> declarations,
            Map<String, String> properties
    ) {
        for (String value : stringArguments(node)) {
            declarations.computeIfAbsent(node.name(), ignored -> new ArrayList<>()).add(value);
        }
        for (var entry : node.properties()) {
            List<KdlValue<?>> values = entry.getValue();
            if (!values.isEmpty()) {
                properties.put(node.name() + "." + entry.getKey(), valueToString(values.get(values.size() - 1)));
            }
        }
    }

    private static String firstArgument(KdlNode node, String fallback) {
        return stringArguments(node).stream().findFirst().orElse(fallback);
    }

    private static List<String> stringArguments(KdlNode node) {
        return node.arguments().stream()
                .map(KdlModMetadataReader::valueToString)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private static String valueToString(KdlValue<?> value) {
        Object raw = value.value();
        return raw == null ? "" : String.valueOf(raw);
    }
}
