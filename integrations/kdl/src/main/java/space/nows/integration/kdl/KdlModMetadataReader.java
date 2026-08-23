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

package space.nows.integration.kdl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.JarFile;

import dev.kdl.KdlDocument;
import dev.kdl.KdlNode;
import dev.kdl.KdlValue;
import dev.kdl.parse.KdlParseException;
import dev.kdl.parse.KdlParser;
import space.nows.platform.api.NowsSide;
import space.nows.platform.core.mod.ModDescriptor;
import space.nows.platform.core.mod.ModDependency;
import space.nows.platform.core.mod.ModMetadataReader;

public final class KdlModMetadataReader implements ModMetadataReader {
    public static final String METADATA_PATH = "nows.mod.kdl";

    @Override
    public Optional<ModDescriptor> read(Path jarPath) throws IOException {
        Objects.requireNonNull(jarPath, "jarPath");
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
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(source, "source");
        KdlNode mod = document.nodes().stream().filter(n -> n.name().equals("mod")).findFirst()
                .orElseThrow(() -> new IOException("Missing root 'mod' node in " + source));
        String id = requiredProperty(mod, "id", source).trim();
        if (!id.matches("[a-z][a-z0-9_-]{1,63}")) throw new IOException("Invalid Nows mod id '" + id + "' in " + source);
        String name = property(mod, "name", id);
        String version = requiredProperty(mod, "version", source).trim();
        String minecraft = property(mod, "minecraft", "*");
        NowsSide side;
        try {
            side = NowsSide.parse(property(mod, "side", property(mod, "environment", "both")));
        } catch (IllegalArgumentException e) {
            throw new IOException(e.getMessage() + " in " + source, e);
        }
        MetadataAccumulator metadata = new MetadataAccumulator();
        metadata.description = property(mod, "description", "");
        metadata.icon = property(mod, "icon", "");
        metadata.authors.addAll(propertyValues(mod, "author"));
        metadata.authors.addAll(propertyValues(mod, "authors"));
        metadata.contributors.addAll(propertyValues(mod, "contributor"));
        metadata.contributors.addAll(propertyValues(mod, "contributors"));
        metadata.licenses.addAll(propertyValues(mod, "license"));
        metadata.licenses.addAll(propertyValues(mod, "licenses"));
        metadata.properties.putAll(rootProperties(mod));
        for (KdlNode child : mod.children()) {
            readMetadataNode(child, metadata);
        }
        if (metadata.declarations.isEmpty()) throw new IOException("Mod " + id + " has no declarations");
        return new ModDescriptor(id, name, version, minecraft, side, metadata.description,
                metadata.authors, metadata.contributors, metadata.licenses, metadata.icon,
                metadata.contacts, metadata.properties, metadata.dependencies, metadata.declarations);
    }

    private static void readMetadataNode(KdlNode node, MetadataAccumulator metadata) {
        switch (node.name()) {
            case "info", "metadata" -> readMetadataChildren(node, metadata);
            case "compatibility", "dependencies", "requirements", "load-order", "ordering",
                    "runtime", "entrypoints", "mixins", "listeners", "events", "features", "declarations" ->
                    readMetadataChildren(node, metadata);
            case "network" -> {
                if (hasChildren(node)) {
                    readMetadataChildren(node, metadata);
                } else {
                    readDeclaration(node, metadata.declarations, metadata.properties);
                }
            }
            case "description" -> metadata.description = firstArgument(node, metadata.description);
            case "author", "authors" -> metadata.authors.addAll(stringArguments(node));
            case "contributor", "contributors" -> metadata.contributors.addAll(stringArguments(node));
            case "license", "licenses" -> metadata.licenses.addAll(stringArguments(node));
            case "icon" -> metadata.icon = firstArgument(node, metadata.icon);
            case "contact", "contacts", "links" -> {
                if (hasChildren(node)) {
                    readContactChildren(node, metadata.contacts);
                } else {
                    readContactNode(node, metadata.contacts);
                }
            }
            case "homepage", "website", "sources", "source", "issues", "wiki", "discord", "email" ->
                    metadata.contacts.put(contactKey(node.name()), firstArgument(node, ""));
            case "property" -> readPropertyNode(node, metadata.properties);
            case "properties", "custom" -> {
                if (hasChildren(node)) {
                    readPropertiesGroup(node, metadata.properties);
                } else {
                    readPropertyNode(node, metadata.properties);
                }
            }
            case "depends", "dependency", "requires", "require", "recommends", "suggests",
                    "breaks", "conflicts", "conflict", "incompatible", "incompatible-with",
                    "load-before", "load-after", "before", "after" ->
                    metadata.dependencies.add(readDependency(node));
            default -> readDeclaration(node, metadata.declarations, metadata.properties);
        }
    }

    private static void readMetadataChildren(KdlNode group, MetadataAccumulator metadata) {
        for (KdlNode child : group.children()) {
            readMetadataNode(child, metadata);
        }
    }

    private static String requiredProperty(KdlNode node, String key, Path source) throws IOException {
        String value = property(node, key, null);
        if (value == null || value.isBlank()) throw new IOException("Missing property '" + key + "' in " + source);
        return value;
    }

    private static String property(KdlNode node, String key, String fallback) {
        return node.<Object>getProperty(key).map(KdlValue::value).map(String::valueOf).map(String::trim).orElse(fallback);
    }

    private static List<String> propertyValues(KdlNode node, String key) {
        return node.properties().getValues(key).stream()
                .map(KdlValue::value)
                .map(String::valueOf)
                .map(String::trim)
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
                    "side", "environment", "author", "authors", "contributor", "contributors", "license", "licenses" -> true;
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

    private static void readContactChildren(KdlNode group, Map<String, String> contacts) {
        for (KdlNode child : group.children()) {
            if (child.name().equals("contact")) {
                readContactNode(child, contacts);
            } else {
                contacts.put(contactKey(child.name()), firstArgument(child, ""));
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

    private static void readPropertiesGroup(KdlNode group, Map<String, String> properties) {
        for (KdlNode child : group.children()) {
            if (child.name().equals("property")) {
                readPropertyNode(child, properties);
                continue;
            }
            String value = firstArgument(child, "");
            if (!value.isBlank()) {
                properties.put(child.name(), value);
            }
            for (var entry : child.properties()) {
                List<KdlValue<?>> values = entry.getValue();
                if (!values.isEmpty()) {
                    properties.put(child.name() + "." + entry.getKey(), valueToString(values.get(values.size() - 1)));
                }
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

    private static boolean hasChildren(KdlNode node) {
        return !node.children().isEmpty();
    }

    private static List<String> stringArguments(KdlNode node) {
        return node.arguments().stream()
                .map(KdlModMetadataReader::valueToString)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private static String valueToString(KdlValue<?> value) {
        Object raw = value.value();
        return raw == null ? "" : String.valueOf(raw).trim();
    }

    private static final class MetadataAccumulator {
        private String description = "";
        private String icon = "";
        private final List<String> authors = new ArrayList<>();
        private final List<String> contributors = new ArrayList<>();
        private final List<String> licenses = new ArrayList<>();
        private final Map<String, String> contacts = new LinkedHashMap<>();
        private final Map<String, String> properties = new LinkedHashMap<>();
        private final List<ModDependency> dependencies = new ArrayList<>();
        private final Map<String, List<String>> declarations = new LinkedHashMap<>();
    }
}
