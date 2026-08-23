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

/** Format-neutral dependency declaration from mod metadata. */
public record ModDependency(
        String kind,
        String id,
        String version,
        boolean optional,
        String reason
) {
    public ModDependency(String id, String version, boolean optional, String reason) {
        this("depends", id, version, optional, reason);
    }

    public ModDependency {
        kind = kind == null || kind.isBlank() ? "depends" : ModDescriptor.normalizeId(kind);
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Dependency id must not be blank");
        }
        id = ModDescriptor.normalizeId(id);
        version = version == null || version.isBlank() ? "*" : version.trim();
        reason = reason == null ? "" : reason.trim();
    }

    public boolean required() {
        return !optional && switch (kind) {
            case "depends", "dependency", "requires", "require" -> true;
            default -> false;
        };
    }

    public boolean conflict() {
        return switch (kind) {
            case "breaks", "conflicts", "conflict", "incompatible", "incompatible-with" -> true;
            default -> false;
        };
    }

    public boolean loadBefore() {
        return switch (kind) {
            case "load-before", "before" -> true;
            default -> false;
        };
    }

    public boolean loadAfter() {
        return switch (kind) {
            case "load-after", "after" -> true;
            default -> false;
        };
    }

    public boolean orderOnly() {
        return loadBefore() || loadAfter();
    }
}
