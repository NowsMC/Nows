import java.security.MessageDigest

plugins { base }

val nowsVersion = providers.gradleProperty("nows_version")
val minecraftVersion = providers.gradleProperty("minecraft_version")
val nowsReleaseBaseUrl = providers.gradleProperty("nows_release_base_url")

allprojects {
    group = "space.nows.mcnows"
    version = nowsVersion.get()

    repositories {
        mavenCentral()
        maven("https://repo.spongepowered.org/repository/maven-public/")

        // KDL4J 1.0.1 is a GitHub Package. Only build-time resolution uses credentials.
        // NowsInstaller embeds the original JAR and extracts it into .minecraft/libraries,
        // so end users never need a GitHub token.
        maven {
            name = "KDL4JGitHubPackages"
            url = uri("https://maven.pkg.github.com/kdl-org/kdl4j")
            credentials {
                username = providers.gradleProperty("gpr.user")
                    .orElse(providers.environmentVariable("GITHUB_ACTOR"))
                    .orNull ?: "x-access-token"
                password = providers.gradleProperty("gpr.token")
                    .orElse(providers.environmentVariable("GITHUB_TOKEN"))
                    .orNull ?: ""
            }
            content { includeGroup("dev.kdl") }
        }
    }
}

subprojects {
    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(25))
            withSourcesJar()
        }
        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release.set(25)
        }
        tasks.withType<AbstractArchiveTask>().configureEach {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
        }
    }
}

tasks.register("dist") {
    group = "nows"
    description = "Builds all modular Nows artifacts, the installer, Gradle plugin and example mod."
    dependsOn(
        ":core:jar",
        ":minecraft:jar",
        ":mc:${minecraftVersion.get()}:jar",
        ":integrations:kdl:jar",
        ":integrations:geb:jar",
        ":integrations:logging:jar",
        ":integrations:mixin:jar",
        ":runtime:jar",
        ":repos:NowsInstaller:jar",
        ":repos:NowsInstaller:guiJar",
        ":repos:NowsInstaller:offlineJar",
        ":repos:NowsGradlePlugin:jar",
        ":repos:NowsApiMod:jar",
        ":example-mod:jar"
    )
}

fun sha256(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}

fun publishedModuleArtifactsFor(minecraft: String): Map<String, String> = mapOf(
    ":core" to "space/nows/mcnows/nows-core/${project.version}/nows-core-${project.version}.jar",
    ":minecraft" to "space/nows/mcnows/nows-minecraft/${project.version}/nows-minecraft-${project.version}.jar",
    ":mc:$minecraft" to "space/nows/mcnows/nows-mc-$minecraft/${project.version}/nows-mc-$minecraft-${project.version}.jar",
    ":integrations:kdl" to "space/nows/mcnows/nows-integration-kdl/${project.version}/nows-integration-kdl-${project.version}.jar",
    ":integrations:geb" to "space/nows/mcnows/nows-integration-geb/${project.version}/nows-integration-geb-${project.version}.jar",
    ":integrations:logging" to "space/nows/mcnows/nows-integration-logging/${project.version}/nows-integration-logging-${project.version}.jar",
    ":integrations:mixin" to "space/nows/mcnows/nows-integration-mixin/${project.version}/nows-integration-mixin-${project.version}.jar",
    ":runtime" to "space/nows/mcnows/nows-runtime/${project.version}/nows-runtime-${project.version}.jar"
)

val publishedMavenArtifactPaths = mapOf(
    "core-${providers.gradleProperty("geb_core_version").get()}.jar" to "foo/zaaarf/geb/core/${providers.gradleProperty("geb_core_version").get()}/core-${providers.gradleProperty("geb_core_version").get()}.jar",
    "reactor-core-${providers.gradleProperty("reactor_version").get()}.jar" to "io/projectreactor/reactor-core/${providers.gradleProperty("reactor_version").get()}/reactor-core-${providers.gradleProperty("reactor_version").get()}.jar",
    "reactive-streams-1.0.4.jar" to "org/reactivestreams/reactive-streams/1.0.4/reactive-streams-1.0.4.jar",
    "disruptor-${providers.gradleProperty("disruptor_version").get()}.jar" to "com/lmax/disruptor/${providers.gradleProperty("disruptor_version").get()}/disruptor-${providers.gradleProperty("disruptor_version").get()}.jar",
    "mixin-${providers.gradleProperty("mixin_version").get()}.jar" to "org/spongepowered/mixin/${providers.gradleProperty("mixin_version").get()}/mixin-${providers.gradleProperty("mixin_version").get()}.jar",
    "asm-9.8.jar" to "org/ow2/asm/asm/9.8/asm-9.8.jar",
    "asm-tree-9.8.jar" to "org/ow2/asm/asm-tree/9.8/asm-tree-9.8.jar",
    "asm-commons-9.8.jar" to "org/ow2/asm/asm-commons/9.8/asm-commons-9.8.jar",
    "asm-analysis-9.8.jar" to "org/ow2/asm/asm-analysis/9.8/asm-analysis-9.8.jar",
    "asm-util-9.8.jar" to "org/ow2/asm/asm-util/9.8/asm-util-9.8.jar"
)

val publishLayoutDir = layout.projectDirectory.dir(".publishing")
val publishLayout by tasks.registering {
    group = "nows"
    description = "Builds the local upload layout for files.nows.space."
    dependsOn(
        ":repos:NowsInstaller:jar",
        ":repos:NowsInstaller:guiJar",
        ":repos:NowsInstaller:offlineJar",
        ":repos:NowsGradlePlugin:jar",
        ":repos:NowsApiMod:jar"
    )

    inputs.file("repos/NowsInstaller/install.properties.template")
    inputs.property("nowsVersion", nowsVersion)
    inputs.property("minecraftVersion", minecraftVersion)
    inputs.property("nowsReleaseBaseUrl", nowsReleaseBaseUrl)
    outputs.dir(publishLayoutDir)

    doLast {
        val nows = nowsVersion.get()
        val minecraft = minecraftVersion.get()
        val releaseRoot = publishLayoutDir.dir("releases/nows/$nows/$minecraft").asFile
        val librariesRoot = releaseRoot.resolve("libraries")
        val installersRoot = releaseRoot.resolve("installers")
        val modsRoot = releaseRoot.resolve("mods")
        val toolingRoot = releaseRoot.resolve("tooling")
        val artifactFiles = linkedMapOf<String, File>()

        delete(releaseRoot)
        librariesRoot.mkdirs()
        installersRoot.mkdirs()
        modsRoot.mkdirs()
        toolingRoot.mkdirs()

        val moduleArtifacts = publishedModuleArtifactsFor(minecraft)
        moduleArtifacts.forEach { (projectPath, relativePath) ->
            val source = project(projectPath).tasks.named<Jar>("jar").get().archiveFile.get().asFile
            val target = librariesRoot.resolve(relativePath)
            target.parentFile.mkdirs()
            source.copyTo(target, overwrite = true)
            artifactFiles[relativePath] = target
        }

        val installerProject = project(":repos:NowsInstaller")
        val offlineMavenArtifacts = installerProject.configurations.getByName("offlineMavenArtifacts").resolve()
        offlineMavenArtifacts.forEach { source ->
            val relativePath = publishedMavenArtifactPaths[source.name]
                ?: throw GradleException("No publish path registered for ${source.name}")
            val target = librariesRoot.resolve(relativePath)
            target.parentFile.mkdirs()
            source.copyTo(target, overwrite = true)
            artifactFiles[relativePath] = target
        }

        val cliInstaller = installerProject.tasks.named<Jar>("jar").get().archiveFile.get().asFile
        cliInstaller.copyTo(installersRoot.resolve(cliInstaller.name), overwrite = true)
        val uiInstaller = installerProject.tasks.named<Jar>("guiJar").get().archiveFile.get().asFile
        uiInstaller.copyTo(installersRoot.resolve(uiInstaller.name), overwrite = true)
        val offlineInstaller = installerProject.tasks.named<Jar>("offlineJar").get().archiveFile.get().asFile
        offlineInstaller.copyTo(
            installersRoot.resolve("NowsInstaller-offline-$nows-mc-$minecraft.jar"),
            overwrite = true
        )

        val apiMod = project(":repos:NowsApiMod").tasks.named<Jar>("jar").get().archiveFile.get().asFile
        apiMod.copyTo(modsRoot.resolve(apiMod.name), overwrite = true)
        val gradlePlugin = project(":repos:NowsGradlePlugin").tasks.named<Jar>("jar").get().archiveFile.get().asFile
        gradlePlugin.copyTo(toolingRoot.resolve(gradlePlugin.name), overwrite = true)

        val template = java.util.Properties()
        file("repos/NowsInstaller/install.properties.template").inputStream().use(template::load)
        template["nows.version"] = nows
        template["minecraft.version"] = minecraft
        val releaseBaseUrl = "${nowsReleaseBaseUrl.get()}/$nows/$minecraft"
        template["releaseBaseUrl"] = releaseBaseUrl
        template["artifact.18.coordinate"] = "space.nows.mcnows:nows-mc-$minecraft:$nows"
        template["artifact.18.path"] = "space/nows/mcnows/nows-mc-$minecraft/$nows/nows-mc-$minecraft-$nows.jar"
        val count = template.getProperty("artifact.count").toInt()
        for (index in 0 until count) {
            val prefix = "artifact.$index."
            val source = template.getProperty(prefix + "source", "internet")
            if (source == "embedded") {
                continue
            }
            val relativePath = template.getProperty(prefix + "path")
            val artifactFile = artifactFiles[relativePath]
                ?: throw GradleException("No staged artifact for $relativePath")
            template[prefix + "url"] = "$releaseBaseUrl/libraries/$relativePath"
            template[prefix + "sha256"] = sha256(artifactFile)
        }

        releaseRoot.resolve("install.properties").bufferedWriter().use { writer ->
            writer.appendLine("# Generated by ./gradlew publishLayout")
            writer.appendLine("# Upload this directory to $releaseBaseUrl")
            template.stringPropertyNames().sortedWith(compareBy<String> {
                it.substringBefore('.')
            }.thenBy { key ->
                key.split('.').getOrNull(1)?.toIntOrNull() ?: -1
            }.thenBy { it }).forEach { key ->
                writer.append(key).append('=').append(template.getProperty(key)).appendLine()
            }
        }

        releaseRoot.resolve("SHA256SUMS").bufferedWriter().use { writer ->
            releaseRoot.walkTopDown()
                .filter { it.isFile && it.name != "SHA256SUMS" }
                .sortedBy { it.relativeTo(releaseRoot).invariantSeparatorsPath }
                .forEach { file ->
                    writer.append(sha256(file))
                        .append("  ")
                        .append(file.relativeTo(releaseRoot).invariantSeparatorsPath)
                        .appendLine()
                }
        }
    }
}

tasks.register("allJar") {
    group = "nows"
    description = "Builds the optional single-JAR Nows distribution. Modular install remains the default."
    dependsOn(":runtime:allJar")
}
