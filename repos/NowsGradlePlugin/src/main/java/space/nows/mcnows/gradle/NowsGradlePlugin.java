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
 * compile-time Nows/Mixin/GEB wiring. It is deliberately a separate repo module
 * so Gradle changes never require changing nows-core.</p>
 */
public final class NowsGradlePlugin implements Plugin<Project> {
    private static final String GEB_CORE = "foo.zaaarf.geb:core:0.5.4";
    private static final String GEB_PROCESSOR = "foo.zaaarf.geb:processor:0.4.9";
    private static final String MIXIN = "net.fabricmc:sponge-mixin:0.17.3+mixin.0.8.7";

    @Override
    public void apply(Project project) {
        NowsExtension extension = project.getExtensions().create("nows", NowsExtension.class);

        ensureRepository(project, "Nows", "https://nows.space/maven/releases");
        ensureRepository(project, "Fabric", "https://maven.fabricmc.net/");
        project.getRepositories().mavenCentral();

        var prepare = project.getTasks().register("nowsPrepareMinecraft", PrepareMinecraftTask.class, task -> {
            task.setGroup("nows");
            task.setDescription("Downloads Minecraft and prepares the official Mojang-mapped development JAR.");
            task.getMinecraftVersion().set(extension.getMinecraftVersion());
            task.getOfficialMappings().set(extension.getOfficialMappings());
            task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("nows/minecraft"));
        });

        project.getPlugins().withType(JavaPlugin.class, ignored -> {
            project.getDependencies().add("compileOnly", project.files(
                    prepare.flatMap(PrepareMinecraftTask::getDevelopmentClientJar)));

            project.getTasks().withType(JavaCompile.class).configureEach(task -> task.dependsOn(prepare));

            project.afterEvaluate(p -> {
                p.getDependencies().add("compileOnly",
                        "space.nows.mcnows:nows-core:" + extension.getNowsVersion().get());

                if (extension.getAddGeb().get()) {
                    p.getDependencies().add("compileOnly", GEB_CORE);
                    p.getDependencies().add("annotationProcessor", GEB_PROCESSOR);
                }
                if (extension.getAddMixin().get()) {
                    p.getDependencies().add("compileOnly", MIXIN);
                    p.getDependencies().add("annotationProcessor", MIXIN);
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
