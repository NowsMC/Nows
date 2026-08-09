import java.security.MessageDigest
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.plugins.signing.SigningExtension

plugins { base }

val nowsVersion = providers.gradleProperty("nows_version")
val minecraftVersion = providers.gradleProperty("minecraft_version")
val nowsReleaseBaseUrl = providers.gradleProperty("nows_release_base_url")
val publishLayoutDir = layout.projectDirectory.dir(".publishing")
val publishingMavenDir = publishLayoutDir.dir("maven")

val cleanPublishingMavenLayout by tasks.registering(Delete::class) {
    delete(publishingMavenDir)
}

val mavenPublishedProjectPaths = setOf(
    ":core",
    ":minecraft",
    ":mc:26.2",
    ":mc:1.20.1",
    ":integrations:kdl",
    ":integrations:geb",
    ":integrations:logging",
    ":integrations:mixin",
    ":runtime"
)

fun publicArtifactId(projectPath: String): String = when (projectPath) {
    ":core" -> "nows-core"
    ":minecraft" -> "nows-minecraft"
    ":mc:26.2" -> "nows-mc-26.2"
    ":mc:1.20.1" -> "nows-mc-1.20.1"
    ":integrations:kdl" -> "nows-integration-kdl"
    ":integrations:geb" -> "nows-integration-geb"
    ":integrations:logging" -> "nows-integration-logging"
    ":integrations:mixin" -> "nows-integration-mixin"
    ":runtime" -> "nows-runtime"
    ":repos:NowsGradlePlugin" -> "nows-gradle-plugin"
    else -> throw GradleException("No public artifact id registered for $projectPath")
}

fun MavenPublication.configureNowsPom(projectPath: String) {
    pom {
        name.set(publicArtifactId(projectPath))
        description.set("Nows Minecraft loader artifact for $projectPath")
        url.set("https://nows.space")
        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }
        developers {
            developer {
                id.set("tamkungz")
                name.set("TamKungZ_")
                email.set("dev@tamkungz.me")
                roles.add("Maintainer")
            }
            developer {
                id.set("hollzaterq")
                name.set("HollZaterQ")
                roles.add("Tester")
            }
        }
        scm {
            connection.set("scm:git:git@github.com:NowsMC/Nows.git")
            developerConnection.set("scm:git:git@github.com:NowsMC/Nows.git")
            url.set("https://github.com/NowsMC/Nows")
        }
    }
}

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

        if (path in mavenPublishedProjectPaths) {
            pluginManager.apply("maven-publish")
            pluginManager.apply("signing")
            plugins.withId("maven-publish") {
                extensions.configure<PublishingExtension> {
                    publications {
                        create<MavenPublication>("mavenJava") {
                            from(components.getByName("java"))
                            artifactId = publicArtifactId(project.path)
                            configureNowsPom(project.path)
                        }
                    }
                    repositories {
                        maven {
                            name = "PublishingMaven"
                            url = rootProject.uri(publishingMavenDir.asFile)
                        }
                    }
                }
                tasks.withType<PublishToMavenRepository>().configureEach {
                    if (name.endsWith("ToPublishingMavenRepository")) {
                        dependsOn(rootProject.tasks.named("cleanPublishingMavenLayout"))
                    }
                }
            }
            plugins.withId("signing") {
                extensions.configure<SigningExtension> {
                    isRequired = true
                    useGpgCmd()
                    sign(extensions.getByType(PublishingExtension::class.java).publications)
                }
            }
        }
    }
}

project(":repos:NowsGradlePlugin") {
    plugins.withId("maven-publish") {
        pluginManager.apply("signing")
        extensions.configure<PublishingExtension> {
            publications.withType<MavenPublication>().configureEach {
                if (name == "pluginMaven") {
                    artifactId = publicArtifactId(project.path)
                }
                configureNowsPom(project.path)
            }
            repositories {
                maven {
                    name = "PublishingMaven"
                    url = rootProject.uri(publishingMavenDir.asFile)
                }
            }
        }
        tasks.withType<PublishToMavenRepository>().configureEach {
            if (name.endsWith("ToPublishingMavenRepository")) {
                dependsOn(rootProject.tasks.named("cleanPublishingMavenLayout"))
            }
        }
        plugins.withId("signing") {
            extensions.configure<SigningExtension> {
                isRequired = true
                useGpgCmd()
                sign(extensions.getByType(PublishingExtension::class.java).publications)
            }
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

fun publishedModuleArtifactIndex(projectPath: String, minecraft: String): Int? = when (projectPath) {
    ":core" -> 0
    ":minecraft" -> 1
    ":integrations:kdl" -> 2
    ":integrations:geb" -> 3
    ":integrations:logging" -> 4
    ":integrations:mixin" -> 5
    ":runtime" -> 6
    ":mc:$minecraft" -> 18
    else -> null
}

fun publishedModuleArtifactId(projectPath: String, minecraft: String): String = when (projectPath) {
    ":core" -> "nows-core"
    ":minecraft" -> "nows-minecraft"
    ":integrations:kdl" -> "nows-integration-kdl"
    ":integrations:geb" -> "nows-integration-geb"
    ":integrations:logging" -> "nows-integration-logging"
    ":integrations:mixin" -> "nows-integration-mixin"
    ":runtime" -> "nows-runtime"
    ":mc:$minecraft" -> "nows-mc-$minecraft"
    else -> throw GradleException("No artifact id registered for $projectPath")
}

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

val publishLayout by tasks.registering {
    group = "nows"
    description = "Builds the local upload layout for files.nows.space."
    dependsOn(
        "publishMavenLayout",
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
        moduleArtifacts.forEach { (projectPath, relativePath) ->
            val index = publishedModuleArtifactIndex(projectPath, minecraft) ?: return@forEach
            val artifactId = publishedModuleArtifactId(projectPath, minecraft)
            template["artifact.$index.coordinate"] = "space.nows.mcnows:$artifactId:$nows"
            template["artifact.$index.path"] = relativePath
        }
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

tasks.register("publishMavenLayout") {
    group = "nows"
    description = "Publishes developer-facing Maven artifacts into .publishing/maven."
    dependsOn(mavenPublishedProjectPaths.map { "$it:publishAllPublicationsToPublishingMavenRepository" })
    dependsOn(":repos:NowsGradlePlugin:publishAllPublicationsToPublishingMavenRepository")
}

fun updateGradleProperty(file: File, key: String, value: String) {
    val lines = file.readLines()
    var replaced = false
    val updated = lines.map { line ->
        if (line.startsWith("$key=")) {
            replaced = true
            "$key=$value"
        } else {
            line
        }
    }.toMutableList()
    if (!replaced) {
        updated.add("$key=$value")
    }
    file.writeText(updated.joinToString(System.lineSeparator()) + System.lineSeparator())
}

tasks.register("versionReport") {
    group = "nows"
    description = "Prints the current Nows release version and default Minecraft target."
    doLast {
        println("Nows version: ${nowsVersion.get()}")
        println("Default Minecraft version: ${minecraftVersion.get()}")
        println("Release base URL: ${nowsReleaseBaseUrl.get()}/${nowsVersion.get()}/${minecraftVersion.get()}")
        println("Standalone NowsApiMod version: "
                + providers.fileContents(layout.projectDirectory.file("repos/NowsApiMod/gradle.properties"))
                    .asText.get().lineSequence()
                    .firstOrNull { it.startsWith("nows_version=") }
                    ?.substringAfter("=")
                    .orEmpty())
    }
}

tasks.register("setNowsVersion") {
    group = "nows"
    description = "Updates Nows version properties. Usage: ./gradlew setNowsVersion -Pnew_nows_version=0.5.0"
    doLast {
        val requested = providers.gradleProperty("new_nows_version").orNull
            ?: throw GradleException("Missing -Pnew_nows_version=<version>")
        require(Regex("""\d+\.\d+\.\d+(-[A-Za-z0-9_.-]+)?""").matches(requested)) {
            "Invalid Nows version: $requested"
        }
        updateGradleProperty(file("gradle.properties"), "nows_version", requested)
        val apiModProperties = file("repos/NowsApiMod/gradle.properties")
        if (apiModProperties.isFile) {
            updateGradleProperty(apiModProperties, "nows_version", requested)
        }
        println("Updated Nows version to $requested")
    }
}

tasks.register("allJar") {
    group = "nows"
    description = "Builds the optional single-JAR Nows distribution. Modular install remains the default."
    dependsOn(":runtime:allJar")
}
