package space.nows.mcnows.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;

/**
 * Keeps Minecraft/Gradle churn outside the loader core.
 *
 * <p>This plugin owns the development Minecraft JAR, Mojang mappings and the
 * compile-time Nows/Mixin/GEB/network wiring. It is deliberately a separate repo module
 * so Gradle changes never require changing nows-core.</p>
 */
public final class NowsGradlePlugin implements Plugin<Project> {
    private static final String GEB_CORE = "foo.zaaarf.geb:core:0.5.4";
    private static final String GEB_PROCESSOR = "foo.zaaarf.geb:processor:0.4.9";
    private static final String NETTY_BUFFER = "io.netty:netty-buffer:4.2.15.Final";
    private static final String MIXIN = "org.spongepowered:mixin:0.8.7";
    private static final String GSON = "com.google.code.gson:gson:2.14.0";
    private static final String GUAVA = "com.google.guava:guava:33.4.8-jre";
    private static final String ASM = "org.ow2.asm:asm:9.8";
    private static final String ASM_TREE = "org.ow2.asm:asm-tree:9.8";
    private static final String ASM_COMMONS = "org.ow2.asm:asm-commons:9.8";
    private static final String ASM_UTIL = "org.ow2.asm:asm-util:9.8";

    @Override
    public void apply(Project project) {
        NowsExtension extension = project.getExtensions().create("nows", NowsExtension.class);

        ensureRepository(project, "Nows", "https://nows.space/maven/releases");
        ensureRepository(project, "SpongePowered", "https://repo.spongepowered.org/repository/maven-public/");
        project.getRepositories().mavenCentral();

        var prepare = project.getTasks().register("nowsPrepareMinecraft", PrepareMinecraftTask.class, task -> {
            task.setGroup("nows");
            task.setDescription("Downloads Minecraft and prepares the official Mojang-mapped development JAR.");
            task.getMinecraftVersion().set(extension.getMinecraftVersion());
            task.getOfficialMappings().set(extension.getOfficialMappings());
            task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("nows/minecraft"));
            task.getDevelopmentClientJar().set(extension.getDevelopmentClientJar().orElse(
                    task.getOutputDirectory().file(extension.getMinecraftVersion().map(v -> v + "/client-dev.jar"))));
        });

        project.getPlugins().withType(JavaPlugin.class, ignored -> {
            project.getDependencies().add("compileOnly", project.files(
                    prepare.flatMap(PrepareMinecraftTask::getDevelopmentClientJar)));

            project.getTasks().withType(JavaCompile.class).configureEach(task -> task.dependsOn(prepare));

            project.afterEvaluate(p -> {
                Project core = p.findProject(":core");
                p.getDependencies().add("compileOnly", core != null
                        ? core
                        : "space.nows.mcnows:nows-core:" + extension.getNowsVersion().get());

                if (extension.getAddGeb().get()) {
                    p.getDependencies().add("compileOnly", GEB_CORE);
                    p.getDependencies().add("annotationProcessor", GEB_PROCESSOR);
                }
                if (extension.getAddNetwork().get()) {
                    Project network = p.findProject(":integrations:network");
                    p.getDependencies().add("compileOnly", network != null
                            ? network
                            : "space.nows.mcnows:nows-integration-network:" + extension.getNowsVersion().get());
                    p.getDependencies().add("compileOnly", NETTY_BUFFER);
                }
                if (extension.getAddMixin().get()) {
                    p.getDependencies().add("compileOnly", MIXIN);
                    p.getDependencies().add("annotationProcessor", MIXIN);
                    p.getDependencies().add("annotationProcessor", GSON);
                    p.getDependencies().add("annotationProcessor", GUAVA);
                    p.getDependencies().add("annotationProcessor", ASM);
                    p.getDependencies().add("annotationProcessor", ASM_TREE);
                    p.getDependencies().add("annotationProcessor", ASM_COMMONS);
                    p.getDependencies().add("annotationProcessor", ASM_UTIL);
                }
            });
        });
    }

    private static void ensureRepository(Project project, String name, String url) {
        boolean exists = false;
        for (var repository : project.getRepositories()) {
            if (repository instanceof MavenArtifactRepository maven) {
                String existing = maven.getUrl().toString();
                if (existing.equals(url) || existing.equals(url + "/")) {
                    exists = true;
                    break;
                }
            }
        }
        if (!exists) {
            project.getRepositories().maven(repo -> {
                repo.setName(name);
                repo.setUrl(url);
            });
        }
    }
}
