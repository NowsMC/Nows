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

package space.nows.platform.core.mod;

import space.nows.platform.api.NowsSide;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Format-neutral mod descriptor. The core owns common mod identity and lookup
 * data while keeping loader features as generic declarations.
 */
public record ModDescriptor(
        String id,
        String name,
        String version,
        String minecraft,
        NowsSide side,
        String description,
        List<String> authors,
        List<String> contributors,
        List<String> licenses,
        String icon,
        Map<String, String> contacts,
        Map<String, String> properties,
        List<ModDependency> dependencies,
        Map<String, List<String>> declarations
) {
    public ModDescriptor(
            String id,
            String name,
            String version,
            String minecraft,
            Map<String, List<String>> declarations
    ) {
        this(id, name, version, minecraft, NowsSide.BOTH, "", List.of(), List.of(), List.of(), "",
                Map.of(), Map.of(), List.of(), declarations);
    }

    public ModDescriptor {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Mod id must not be blank");
        }
        id = id.trim();
        name = name == null || name.isBlank() ? id : name.trim();
        version = version == null || version.isBlank() ? "unknown" : version.trim();
        minecraft = minecraft == null || minecraft.isBlank() ? "*" : minecraft.trim();
        side = side == null ? NowsSide.BOTH : side;
        description = description == null ? "" : description.trim();
        authors = List.copyOf(authors == null ? List.of() : authors);
        contributors = List.copyOf(contributors == null ? List.of() : contributors);
        licenses = List.copyOf(licenses == null ? List.of() : licenses);
        icon = icon == null ? "" : icon.trim();
        contacts = immutableStringMap(contacts);
        properties = immutableStringMap(properties);
        dependencies = List.copyOf(dependencies == null ? List.of() : dependencies);

        Map<String, List<String>> copy = new LinkedHashMap<>();
        if (declarations != null) {
            declarations.forEach((key, value) -> {
                if (key != null && !key.isBlank()) {
                    copy.put(key.trim(), List.copyOf(value == null ? List.of() : value));
                }
            });
        }
        declarations = Collections.unmodifiableMap(copy);
    }

    public List<String> declarations(String key) {
        return declarations.getOrDefault(key, List.of());
    }

    public Optional<String> declaration(String key) {
        List<String> values = declarations(key);
        return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }

    public boolean hasDeclaration(String key) {
        return !declarations(key).isEmpty();
    }

    public Optional<String> contact(String key) {
        return Optional.ofNullable(contacts.get(key));
    }

    public Optional<String> property(String key) {
        return Optional.ofNullable(properties.get(key));
    }

    public List<ModDependency> dependencies(String id) {
        return dependencies.stream().filter(dependency -> dependency.id().equals(id)).toList();
    }

    public List<ModDependency> requiredDependencies() {
        return dependencies.stream().filter(dependency -> !dependency.optional()).toList();
    }

    public List<ModDependency> optionalDependencies() {
        return dependencies.stream().filter(ModDependency::optional).toList();
    }

    private static Map<String, String> immutableStringMap(Map<String, String> input) {
        Map<String, String> copy = new LinkedHashMap<>();
        if (input != null) {
            input.forEach((key, value) -> {
                if (key != null && !key.isBlank() && value != null) {
                    copy.put(key.trim(), value.trim());
                }
            });
        }
        return Collections.unmodifiableMap(copy);
    }
}
