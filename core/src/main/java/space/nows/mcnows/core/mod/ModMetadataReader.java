package space.nows.mcnows.core.mod;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/** Metadata format SPI. KDL is an integration rather than a core dependency. */
@FunctionalInterface
public interface ModMetadataReader {
    Optional<ModDescriptor> read(Path jar) throws IOException;
}
