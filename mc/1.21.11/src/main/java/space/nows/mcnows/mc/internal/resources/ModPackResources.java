package space.nows.mcnows.mc.internal.resources;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.stream.Stream;

final class ModPackResources implements PackResources {
    private final PackLocationInfo location;
    private final Path root;
    private final ZipFile zip;

    ModPackResources(PackLocationInfo location, Path root) throws IOException {
        this.location = location;
        this.root = root;
        this.zip = Files.isDirectory(root) ? null : new ZipFile(root.toFile());
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        String path = String.join("/", elements);
        if (zip == null) {
            Path file = root.resolve(path);
            return Files.isRegularFile(file) ? () -> Files.newInputStream(file) : null;
        }
        ZipEntry entry = zip.getEntry(path);
        return entry == null ? null : IoSupplier.create(zip, entry);
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, Identifier id) {
        String path = type.getDirectory() + "/" + id.getNamespace() + "/" + id.getPath();
        if (zip == null) {
            Path file = root.resolve(path);
            return Files.isRegularFile(file) ? () -> Files.newInputStream(file) : null;
        }
        ZipEntry entry = zip.getEntry(path);
        return entry == null ? null : IoSupplier.create(zip, entry);
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        String root = type.getDirectory() + "/" + namespace + "/";
        String prefix = root + (path.isBlank() ? "" : path + "/");
        if (zip == null) {
            Path directory = this.root.resolve(prefix);
            if (!Files.isDirectory(directory)) {
                return;
            }
            try (Stream<Path> paths = Files.walk(directory)) {
                paths.filter(Files::isRegularFile)
                        .forEach(file -> {
                            String name = this.root.relativize(file).toString().replace('\\', '/');
                            String resourcePath = name.substring(root.length());
                            Identifier id = Identifier.tryBuild(namespace, resourcePath);
                            if (id != null) {
                                output.accept(id, () -> Files.newInputStream(file));
                            }
                        });
            } catch (IOException ignored) {
            }
            return;
        }
        zip.stream()
                .filter(entry -> !entry.isDirectory())
                .map(ZipEntry::getName)
                .filter(name -> name.startsWith(prefix))
                .forEach(name -> {
                    String resourcePath = name.substring(root.length());
                    Identifier id = Identifier.tryBuild(namespace, resourcePath);
                    if (id == null) {
                        return;
                    }
                    ZipEntry entry = zip.getEntry(name);
                    output.accept(id, IoSupplier.create(zip, entry));
                });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        String root = type.getDirectory() + "/";
        Set<String> namespaces = new HashSet<>();
        if (zip == null) {
            Path directory = this.root.resolve(root);
            if (!Files.isDirectory(directory)) {
                return namespaces;
            }
            try (Stream<Path> paths = Files.list(directory)) {
                paths.filter(Files::isDirectory)
                        .map(path -> path.getFileName().toString())
                        .filter(namespace -> Identifier.isValidNamespace(namespace))
                        .forEach(namespaces::add);
            } catch (IOException ignored) {
            }
            return namespaces;
        }
        zip.stream()
                .map(ZipEntry::getName)
                .filter(name -> name.startsWith(root))
                .map(name -> name.substring(root.length()))
                .filter(name -> name.indexOf('/') > 0)
                .map(name -> name.substring(0, name.indexOf('/')))
                .filter(namespace -> Identifier.isValidNamespace(namespace))
                .forEach(namespaces::add);
        return namespaces;
    }

    @Override
    public <T> T getMetadataSection(net.minecraft.server.packs.metadata.MetadataSectionType<T> type) {
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return location;
    }

    @Override
    public void close() {
        if (zip != null) {
            try {
                zip.close();
            } catch (IOException ignored) {
            }
        }
    }
}
