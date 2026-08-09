package space.nows.mcnows.core.mod;

import java.util.Locale;

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
        kind = kind == null || kind.isBlank() ? "depends" : kind.trim().toLowerCase(Locale.ROOT);
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Dependency id must not be blank");
        }
        id = id.trim().toLowerCase(Locale.ROOT);
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
