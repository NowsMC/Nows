package space.nows.mcnows.core.classloading;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NowsClassLoaderTest {
    @Test
    void startsWithStableJdkParentFirstRules() throws Exception {
        try (NowsClassLoader loader = new NowsClassLoader(new java.net.URL[0], getClass().getClassLoader())) {
            assertTrue(loader.loadClass("java.lang.String") == String.class);
        }
    }
}
