package space.nows.mcnows.core.classloading;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NowsClassLoaderTest {
    private static final String PROBE_CLASS = "space.nows.mcnows.core.classloading.CodeSourceProbe";
    private static final String PROBE_RESOURCE = PROBE_CLASS.replace('.', '/') + ".class";

    @Test
    void startsWithStableJdkParentFirstRules() throws Exception {
        try (NowsClassLoader loader = new NowsClassLoader(new java.net.URL[0], getClass().getClassLoader())) {
            assertTrue(loader.loadClass("java.lang.String") == String.class);
        }
    }

    @Test
    void preservesDirectoryCodeSource() throws Exception {
        URL classes = CodeSourceProbe.class.getProtectionDomain().getCodeSource().getLocation();

        try (NowsClassLoader loader = new NowsClassLoader(new URL[]{classes}, ClassLoader.getPlatformClassLoader())) {
            Class<?> loaded = loader.loadClass(PROBE_CLASS);

            assertEquals(classes.toURI(), loaded.getProtectionDomain().getCodeSource().getLocation().toURI());
        }
    }

    @Test
    void preservesJarCodeSource(@TempDir Path tempDirectory) throws Exception {
        Path jar = tempDirectory.resolve("probe.jar");
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(PROBE_RESOURCE);
             JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry(PROBE_RESOURCE));
            output.write(input.readAllBytes());
            output.closeEntry();
        }

        try (NowsClassLoader loader = new NowsClassLoader(
                new URL[]{jar.toUri().toURL()}, ClassLoader.getPlatformClassLoader())) {
            Class<?> loaded = loader.loadClass(PROBE_CLASS);

            assertEquals(jar.toUri(), loaded.getProtectionDomain().getCodeSource().getLocation().toURI());
        }
    }

    @Test
    void childOnlyPackagesDoNotFallBackToParent() throws Exception {
        URL classes = CodeSourceProbe.class.getProtectionDomain().getCodeSource().getLocation();

        try (URLClassLoader parent = new URLClassLoader(new URL[]{classes}, ClassLoader.getPlatformClassLoader());
             NowsClassLoader loader = new NowsClassLoader(new URL[0], parent)) {
            loader.addChildOnlyPrefix("space.nows.mcnows.core.classloading.");

            assertThrows(ClassNotFoundException.class, () -> loader.loadClass(PROBE_CLASS));
        }
    }

    @Test
    void childClassDefinitionFailureDoesNotFallBackToParent(@TempDir Path tempDirectory) throws Exception {
        Path jar = tempDirectory.resolve("broken-probe.jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry(PROBE_RESOURCE));
            output.write(new byte[]{0, 1, 2, 3});
            output.closeEntry();
        }

        try (NowsClassLoader loader = new NowsClassLoader(
                new URL[]{jar.toUri().toURL()}, getClass().getClassLoader())) {
            assertThrows(LinkageError.class, () -> loader.loadClass(PROBE_CLASS));
        }
    }
}
