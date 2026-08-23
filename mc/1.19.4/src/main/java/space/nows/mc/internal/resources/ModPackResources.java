/*
 * Copyright 2026 TamKungZ_ (Nows MC — https://nows.space)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.nows.mc.internal.resources;

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

final class ModPackResources implements PackResources {
    private final String id;
    private final ZipFile zip;

    ModPackResources(String id, Path jar) throws IOException {
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
    @SuppressWarnings("unchecked")
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) {
        if ("pack".equals(serializer.getMetadataSectionName())) {
            return (T) ModPackSource.metadata();
        }
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
