package space.nows.mcnows.core.mod;

import java.nio.file.Path;

public record ModContainer(Path path, ModDescriptor descriptor) {
}
