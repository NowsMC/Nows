package space.nows.mcnows.mc.internal.datagen;

import com.squareup.moshi.Moshi;
import space.nows.mcnows.mc.api.datagen.NowsDataGen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NowsDataGenImpl implements NowsDataGen {
    private final Moshi moshi = new Moshi.Builder().build();
    private final Path outputDirectory;

    public NowsDataGenImpl(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public Moshi moshi() {
        return moshi;
    }

    @Override
    public Path outputDirectory() {
        return outputDirectory;
    }

    @Override
    public Path path(String relativePath) {
        return outputDirectory.resolve(relativePath).normalize();
    }

    @Override
    public void writeText(String relativePath, String text) throws IOException {
        Path file = path(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, text, StandardCharsets.UTF_8);
    }

    @Override
    public void writeJson(String relativePath, String json) throws IOException {
        writeText(relativePath, json.endsWith("\n") ? json : json + "\n");
    }
}
