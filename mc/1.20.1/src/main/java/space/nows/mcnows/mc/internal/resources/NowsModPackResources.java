package space.nows.mcnows.mc.internal.resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

final class NowsModPackResources implements PackResources {
    private final String id;
    private final ZipFile zip;

    NowsModPackResources(String id, Path jar) throws IOException {
        this.id = id;
        this.zip = new ZipFile(jar.toFile());
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        String path = String.join("/", elements);
        ZipEntry entry = zip.getEntry(path);
        return entry == null ? null : IoSupplier.create(zip, entry);
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {
        String path = type.getDirectory() + "/" + id.getNamespace() + "/" + id.getPath();
        ZipEntry entry = zip.getEntry(path);
        return entry == null ? null : IoSupplier.create(zip, entry);
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        String root = type.getDirectory() + "/" + namespace + "/";
        String prefix = root + (path.isBlank() ? "" : path + "/");
        zip.stream()
                .filter(entry -> !entry.isDirectory())
                .map(ZipEntry::getName)
                .filter(name -> name.startsWith(prefix))
                .forEach(name -> {
                    String resourcePath = name.substring(root.length());
                    ResourceLocation id = ResourceLocation.tryParse(namespace + ":" + resourcePath);
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
        zip.stream()
                .map(ZipEntry::getName)
                .filter(name -> name.startsWith(root))
                .map(name -> name.substring(root.length()))
                .filter(name -> name.indexOf('/') > 0)
                .map(name -> name.substring(0, name.indexOf('/')))
                .filter(namespace -> ResourceLocation.tryParse(namespace + ":dummy") != null)
                .forEach(namespaces::add);
        return namespaces;
    }

    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) {
        return null;
    }

    @Override
    public String packId() {
        return id;
    }

    @Override
    public void close() {
        try {
            zip.close();
        } catch (IOException ignored) {
        }
    }
}
