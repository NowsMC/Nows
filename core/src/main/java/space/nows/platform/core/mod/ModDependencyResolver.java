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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/** Validates dependency metadata and returns a deterministic load order. */
public final class ModDependencyResolver {
    private ModDependencyResolver() {
    }

    public static List<ModContainer> resolve(List<ModContainer> mods, Map<String, String> providedVersions)
            throws IOException {
        Map<String, ModContainer> modsById = new LinkedHashMap<>();
        Map<String, Integer> originalOrder = new HashMap<>();
        for (int index = 0; index < mods.size(); index++) {
            ModContainer mod = mods.get(index);
            if (modsById.putIfAbsent(mod.descriptor().id(), mod) != null) {
                throw new IOException("Duplicate Nows mod id '" + mod.descriptor().id() + "'");
            }
            originalOrder.put(mod.descriptor().id(), index);
        }

        Map<String, String> versions = new LinkedHashMap<>();
        if (providedVersions != null) {
            providedVersions.forEach((id, version) -> versions.put(normalizeId(id), version));
        }
        for (ModContainer mod : mods) {
            versions.put(mod.descriptor().id(), mod.descriptor().version());
        }

        Map<String, Set<String>> edges = new LinkedHashMap<>();
        for (ModContainer mod : mods) {
            edges.put(mod.descriptor().id(), new LinkedHashSet<>());
        }

        for (ModContainer mod : mods) {
            validateAndAddEdges(mod, modsById, versions, edges);
        }
        return sort(modsById, originalOrder, edges);
    }

    private static void validateAndAddEdges(
            ModContainer mod,
            Map<String, ModContainer> modsById,
            Map<String, String> versions,
            Map<String, Set<String>> edges
    ) throws IOException {
        String modId = mod.descriptor().id();
        for (ModDependency dependency : mod.descriptor().dependencies()) {
            String targetId = dependency.id();
            String targetVersion = versions.get(targetId);
            boolean targetLoadedMod = modsById.containsKey(targetId);
            boolean targetPresent = targetVersion != null;
            boolean versionMatches = targetPresent
                    && ModVersionConstraint.matches(dependency.version(), targetVersion);

            if (dependency.required()) {
                if (!targetPresent) {
                    throw new IOException("Mod " + modId + " requires " + targetId
                            + describeVersion(dependency) + describeReason(dependency)
                            + " but it is not loaded/provided");
                }
                if (!versionMatches) {
                    throw new IOException("Mod " + modId + " requires " + targetId
                            + describeVersion(dependency) + " but found " + targetVersion
                            + describeReason(dependency));
                }
                if (targetLoadedMod) {
                    addEdge(edges, targetId, modId);
                }
            } else if (dependency.conflict()) {
                if (versionMatches) {
                    throw new IOException("Mod " + modId + " is incompatible with " + targetId
                            + describeVersion(dependency) + describeReason(dependency));
                }
            } else if (!dependency.orderOnly() && targetLoadedMod && versionMatches) {
                addEdge(edges, targetId, modId);
            }

            if (dependency.loadBefore() && targetLoadedMod) {
                addEdge(edges, modId, targetId);
            } else if (dependency.loadAfter() && targetLoadedMod) {
                addEdge(edges, targetId, modId);
            }
        }
    }

    private static List<ModContainer> sort(
            Map<String, ModContainer> modsById,
            Map<String, Integer> originalOrder,
            Map<String, Set<String>> edges
    ) throws IOException {
        Map<String, Integer> incoming = new HashMap<>();
        for (String id : modsById.keySet()) {
            incoming.put(id, 0);
        }
        edges.forEach((from, targets) -> targets.forEach(to -> incoming.computeIfPresent(to, (ignored, count) -> count + 1)));

        PriorityQueue<String> ready = new PriorityQueue<>(Comparator.comparingInt(originalOrder::get));
        incoming.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .forEach(ready::add);
        List<ModContainer> ordered = new ArrayList<>();

        while (!ready.isEmpty()) {
            String id = ready.remove();
            ordered.add(modsById.get(id));
            for (String target : edges.getOrDefault(id, Set.of())) {
                int remaining = incoming.computeIfPresent(target, (ignored, count) -> count - 1);
                if (remaining == 0) {
                    ready.add(target);
                }
            }
        }

        if (ordered.size() != modsById.size()) {
            throw new IOException("Mod load order contains a cycle involving " + cyclicMods(incoming));
        }
        return List.copyOf(ordered);
    }

    private static void addEdge(Map<String, Set<String>> edges, String before, String after) {
        if (!before.equals(after) && edges.containsKey(before) && edges.containsKey(after)) {
            edges.get(before).add(after);
        }
    }

    private static String cyclicMods(Map<String, Integer> incoming) {
        return incoming.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .sorted()
                .toList()
                .toString();
    }

    private static String describeVersion(ModDependency dependency) {
        return dependency.version().equals("*") ? "" : " " + dependency.version();
    }

    private static String describeReason(ModDependency dependency) {
        return dependency.reason().isBlank() ? "" : " (" + dependency.reason() + ")";
    }

    private static String normalizeId(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }
}
