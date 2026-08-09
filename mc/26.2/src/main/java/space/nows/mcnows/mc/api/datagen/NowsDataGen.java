package space.nows.mcnows.mc.api.datagen;

import java.io.IOException;
import java.nio.file.Path;

/** Small generated-data writer for recipes, tags and other JSON assets. */
public interface NowsDataGen {
    Path outputDirectory();

    Path path(String relativePath);

    void writeText(String relativePath, String text) throws IOException;

    void writeJson(String relativePath, String json) throws IOException;

    default String recipePath(String id) {
        return "data/" + namespace(id) + "/recipe/" + pathPart(id) + ".json";
    }

    default String itemTagPath(String id) {
        return "data/" + namespace(id) + "/tags/item/" + pathPart(id) + ".json";
    }

    default String blockTagPath(String id) {
        return "data/" + namespace(id) + "/tags/block/" + pathPart(id) + ".json";
    }

    private static String namespace(String id) {
        int split = id.indexOf(':');
        return split >= 0 ? id.substring(0, split) : "minecraft";
    }

    private static String pathPart(String id) {
        int split = id.indexOf(':');
        return split >= 0 ? id.substring(split + 1) : id;
    }
}
