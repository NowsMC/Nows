package space.nows.mcnows.core.mod;

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
        kind = kind == null || kind.isBlank() ? "depends" : kind.trim();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Dependency id must not be blank");
        }
        id = id.trim();
        version = version == null || version.isBlank() ? "*" : version.trim();
        reason = reason == null ? "" : reason.trim();
    }
}
