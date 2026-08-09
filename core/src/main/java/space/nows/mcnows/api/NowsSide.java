package space.nows.mcnows.api;

import java.util.Locale;

/** Physical runtime side for loader and mod metadata. */
public enum NowsSide {
    CLIENT,
    SERVER,
    BOTH;

    public boolean supports(NowsSide runtimeSide) {
        return this == BOTH || this == runtimeSide;
    }

    public String metadataName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static NowsSide parse(String value) {
        if (value == null || value.isBlank()) {
            return BOTH;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "client" -> CLIENT;
            case "server" -> SERVER;
            case "both", "*", "common", "universal" -> BOTH;
            default -> throw new IllegalArgumentException(
                    "Invalid Nows side '" + value + "'; expected client, server or both");
        };
    }
}
