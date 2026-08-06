package space.nows.mcnows.core.mod;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Format-neutral mod descriptor. The core knows only identity/version fields and
 * generic declarations. New metadata nodes do not require changing nows-core.
 */
public record ModDescriptor(
        String id,
        String name,
        String version,
        String minecraft,
        Map<String, List<String>> declarations
) {
    public ModDescriptor {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        declarations.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        declarations = Map.copyOf(copy);
    }

    public List<String> declarations(String key) {
        return declarations.getOrDefault(key, List.of());
    }
}
