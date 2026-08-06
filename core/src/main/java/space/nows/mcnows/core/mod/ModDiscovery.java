package space.nows.mcnows.core.mod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ModDiscovery {
    private ModDiscovery() {}

    public static List<ModContainer> scan(Path modsDirectory, ModMetadataReader reader) throws IOException {
        Files.createDirectories(modsDirectory);
        List<ModContainer> result = new ArrayList<>();
        Set<String> ids = new HashSet<>();

        try (var paths = Files.list(modsDirectory)) {
            for (Path jar : paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList()) {
                var descriptor = reader.read(jar);
                if (descriptor.isEmpty()) continue;
                if (!ids.add(descriptor.get().id())) {
                    throw new IOException("Duplicate Nows mod id '" + descriptor.get().id() + "' in " + jar);
                }
                result.add(new ModContainer(jar.toAbsolutePath().normalize(), descriptor.get()));
            }
        }
        return List.copyOf(result);
    }
}
