package space.nows.mcnows.integration.network;

import java.util.Locale;

/** Namespaced network channel id such as {@code example:sync}. */
public record NetworkChannelId(String namespace, String path) {
    private static final String PART_PATTERN = "[a-z0-9_.-]+";

    public NetworkChannelId {
        namespace = normalize("namespace", namespace);
        path = normalize("path", path);
    }

    public String value() {
        return namespace + ":" + path;
    }

    @Override
    public String toString() {
        return value();
    }

    public static NetworkChannelId of(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Network channel id must not be blank");
        }
        int separator = id.indexOf(':');
        if (separator <= 0 || separator == id.length() - 1 || id.indexOf(':', separator + 1) != -1) {
            throw new IllegalArgumentException("Network channel id must be namespaced as namespace:path: " + id);
        }
        return new NetworkChannelId(id.substring(0, separator), id.substring(separator + 1));
    }

    private static String normalize(String label, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Network channel " + label + " must not be blank");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches(PART_PATTERN)) {
            throw new IllegalArgumentException("Invalid network channel " + label + ": " + value);
        }
        return normalized;
    }
}
