import java.security.MessageDigest
import java.lang.ProcessBuilder.Redirect
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension

plugins { base }

val reflectionPatterns = listOf(
    "Class.forName(",
    ".getConstructor(",
    ".getDeclaredConstructor(",
    ".getMethod(",
    ".getDeclaredMethod(",
    ".getField(",
    ".getDeclaredField(",
    "setAccessible("
)

val productionReflectionAllowlist = setOf(
    "runtime/src/main/java/space/nows/loader/runtime/NowsLauncher.java",
    "integrations/geb/src/main/java/space/nows/integration/geb/GebIntegration.java",
    "integrations/mixin/src/main/java/space/nows/integration/mixin/NowsMixinBootstrap.java",
    "integrations/mixin/src/main/java/space/nows/integration/mixin/NowsMixinService.java",
    "repos/NowsMCreatorGenerator/src/main/java/space/nows/mcreator/NowsMCreatorPlugin.java"
)

val productionReflectionAllowedSuffixes = listOf(
    "/src/main/java/space/nows/mc/internal/registry/RegistryApiImpl.java",
    "/src/main/java/space/nows/mc/internal/client/render/shader/ShaderApiImpl.java",
    "/src/main/java/space/nows/mc/internal/client/render/shader/ShaderReflection.java"
)

val forbidProductionReflection by tasks.registering {
    group = "verification"
    description = "Fails when new production reflection is introduced outside reviewed compatibility boundaries."

    inputs.files(fileTree(layout.projectDirectory) {
        include("**/src/main/java/**/*.java")
        exclude("**/build/**")
    })

    doLast {
        val root = layout.projectDirectory.asFile.toPath()
        val violations = inputs.files.files
            .filter { it.isFile }
            .mapNotNull { file ->
                val relative = root.relativize(file.toPath()).toString().replace(File.separatorChar, '/')
                val allowed = relative in productionReflectionAllowlist ||
                        productionReflectionAllowedSuffixes.any(relative::endsWith)
                if (allowed) {
                    null
                } else {
                    val lines = file.readLines()
                    val hits = lines.mapIndexedNotNull { index, line ->
                        if (reflectionPatterns.any(line::contains)) "${relative}:${index + 1}: ${line.trim()}" else null
                    }
                    hits.takeIf { it.isNotEmpty() }
                }
            }
            .flatten()
        if (violations.isNotEmpty()) {
            throw GradleException("Production reflection is blocked outside the reviewed allowlist:\n" +
                    violations.joinToString("\n"))
        }
    }
}

tasks.named("check") {
    dependsOn(forbidProductionReflection)
}

abstract class GpgSigningService : BuildService<BuildServiceParameters.None>

val nowsVersion = providers.gradleProperty("nows_version")
val minecraftVersion = providers.gradleProperty("minecraft_version")
val nowsReleaseBaseUrl = providers.gradleProperty("nows_release_base_url")
val githubPackageUser = providers.gradleProperty("gpr.user")
    .orElse(providers.environmentVariable("GITHUB_ACTOR"))
val githubPackageToken = providers.gradleProperty("gpr.token")
    .orElse(providers.gradleProperty("gpr.key"))
    .orElse(providers.environmentVariable("GITHUB_TOKEN"))
val publishLayoutDir = layout.projectDirectory.dir(".publishing")
val publishingMavenDir = publishLayoutDir.dir("maven")
val nowsWebDir = layout.projectDirectory.dir("repos/NowsWeb")
val gpgSigningService = gradle.sharedServices.registerIfAbsent("gpgSigningService", GpgSigningService::class) {
    maxParallelUsages.set(1)
}

val mavenPublishedProjectPaths = setOf(
    ":core",
    ":minecraft",
    ":mc:26.2",
    ":mc:26.1.2",
    ":mc:1.21.11",
    ":mc:1.21.1",
    ":mc:1.20.6",
    ":mc:1.20.1",
    ":mc:1.19.4",
    ":mc:1.19.2",
    ":mc:1.18.2",
    ":mc:1.17.1",
    ":mc:1.16.5",
    ":mc:1.16.4",
    ":integrations:kdl",
    ":integrations:geb",
    ":integrations:logging",
    ":integrations:network",
    ":integrations:mixin",
    ":runtime",
    ":repos:NowsRemapper"
)

val publishedMinecraftVersions = mavenPublishedProjectPaths.mapNotNull { projectPath ->
    if (projectPath.startsWith(":mc:")) projectPath.removePrefix(":mc:") else null
}

fun publicArtifactId(projectPath: String): String = when (projectPath) {
    ":core" -> "nows-core"
    ":minecraft" -> "nows-minecraft"
    ":integrations:kdl" -> "nows-integration-kdl"
    ":integrations:geb" -> "nows-integration-geb"
    ":integrations:logging" -> "nows-integration-logging"
    ":integrations:network" -> "nows-integration-network"
    ":integrations:mixin" -> "nows-integration-mixin"
    ":runtime" -> "nows-runtime"
    ":repos:NowsRemapper" -> "nows-remapper"
    ":repos:NowsGradlePlugin" -> "nows-gradle-plugin"
    ":repos:NowsApiMod" -> "nows-api-mod"
    else -> if (projectPath.startsWith(":mc:")) {
        "nows-mc-" + projectPath.removePrefix(":mc:")
    } else {
        throw GradleException("No public artifact id registered for $projectPath")
    }
}

fun minecraftTaskSuffix(minecraft: String): String =
    "Mc" + minecraft.replace('.', '_')

fun apiModJarTaskName(minecraft: String): String =
    "apiMod${minecraftTaskSuffix(minecraft)}Jar"

fun offlineInstallerJarTaskName(minecraft: String): String =
    if (minecraft == minecraftVersion.get()) {
        "offlineJar"
    } else {
        "offline${minecraftTaskSuffix(minecraft)}Jar"
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
    group = "space.nows"
    version = nowsVersion.get()

    tasks.withType<Sign>().configureEach {
        usesService(gpgSigningService)
    }

    repositories {
        mavenCentral()
        maven("https://repo.spongepowered.org/repository/maven-public/")

        // Prefer the public JitPack KDL4J artifact. GitHub Packages remains configured
        // after it for release environments that still need to resolve legacy coordinates.
        maven {
            name = "JitPack"
            url = uri("https://jitpack.io")
            content { includeGroup("com.github.kdl-org") }
        }
        maven {
            name = "KDL4JGitHubPackages"
            url = uri("https://maven.pkg.github.com/kdl-org/kdl4j")
            credentials {
                username = githubPackageUser.orNull ?: "x-access-token"
                password = githubPackageToken.orNull ?: ""
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
            options.release.set(17)
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
            }
            plugins.withId("signing") {
                extensions.configure<SigningExtension> {
                    isRequired = true
                    useGpgCmd()
                    sign(extensions.getByType(PublishingExtension::class.java).publications)
                }
            }
        }

        plugins.withId("java-library") {
            if (path.startsWith(":mc:")) {
                val minecraftDevJar = layout.buildDirectory.file("nows/minecraft/${project.name}/client-dev.jar")
                extensions.configure<SourceSetContainer> {
                    named("main") {
                        java.srcDir(rootProject.layout.projectDirectory.dir("mc/shared-main/src/main/java"))
                    }
                    named("test") {
                        java.srcDir(rootProject.layout.projectDirectory.dir("mc/shared-test/src/test/java"))
                        compileClasspath += named("main").get().compileClasspath
                        runtimeClasspath += named("main").get().compileClasspath
                    }
                }
                dependencies.add("testImplementation", dependencies.platform("org.junit:junit-bom:5.12.2"))
                dependencies.add("testImplementation", "org.junit.jupiter:junit-jupiter")
                dependencies.add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
                dependencies.add("testRuntimeOnly", "com.mojang:authlib:7.0.61")
                dependencies.add("testRuntimeOnly", "com.mojang:logging:1.0.0")
                dependencies.add("testRuntimeOnly", "org.apache.logging.log4j:log4j-api:2.24.3")
                dependencies.add("testRuntimeOnly", "org.apache.logging.log4j:log4j-core:2.24.3")
                dependencies.add("testRuntimeOnly", "io.netty:netty-codec:${providers.gradleProperty("netty_version").get()}")
                dependencies.add("testRuntimeOnly", "org.joml:joml:1.10.8")
                dependencies.add("testCompileOnly", files(minecraftDevJar))
                dependencies.add("testRuntimeOnly", files(minecraftDevJar))
                tasks.withType<Test>().configureEach {
                    dependsOn(tasks.named("nowsPrepareMinecraft"))
                    classpath = files(minecraftDevJar) + classpath
                    useJUnitPlatform()
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
        plugins.withId("signing") {
            extensions.configure<SigningExtension> {
                isRequired = true
                useGpgCmd()
                sign(extensions.getByType(PublishingExtension::class.java).publications)
            }
        }
    }
}

project(":repos:NowsApiMod") {
    pluginManager.apply("maven-publish")
    pluginManager.apply("signing")
    afterEvaluate {
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("apiMod") {
                    artifactId = publicArtifactId(project.path)
                    publishedMinecraftVersions.forEach { minecraft ->
                        artifact(tasks.named<Jar>(apiModJarTaskName(minecraft))) {
                            classifier = "mc-$minecraft"
                        }
                    }
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
        extensions.configure<SigningExtension> {
            isRequired = true
            useGpgCmd()
            sign(extensions.getByType(PublishingExtension::class.java).publications)
        }
    }
}

tasks.register("dist") {
    group = "nows"
    description = "Builds all modular Nows artifacts, the installer, Gradle plugin and API mod variants."
    dependsOn(
        ":core:jar",
        ":minecraft:jar",
        ":mc:${minecraftVersion.get()}:jar",
        ":integrations:kdl:jar",
        ":integrations:geb:jar",
        ":integrations:logging:jar",
        ":integrations:network:jar",
        ":integrations:mixin:jar",
        ":runtime:jar",
        ":repos:NowsRemapper:jar",
        ":repos:NowsInstaller:jar",
        ":repos:NowsGradlePlugin:jar",
        ":repos:NowsApiMod:allApiModJars"
    )
}

fun commandSucceeds(vararg command: String): Boolean {
    return try {
        ProcessBuilder(*command)
            .redirectOutput(Redirect.DISCARD)
            .redirectError(Redirect.DISCARD)
            .start()
            .waitFor() == 0
    } catch (_: Exception) {
        false
    }
}

fun commandOutput(vararg command: String): String? {
    return try {
        val process = ProcessBuilder(*command)
            .redirectError(Redirect.DISCARD)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        if (process.waitFor() == 0) output else null
    } catch (_: Exception) {
        null
    }
}

val checkWorkspacePrerequisites by tasks.registering {
    group = "nows"
    description = "Checks tools and submodules needed by a fresh Nows workspace."

    doLast {
        val requiredPaths = listOf(
            "repos/NowsInstaller/build.gradle.kts",
            "repos/NowsGradlePlugin/build.gradle.kts",
            "repos/NowsApiMod/build.gradle.kts"
        )
        val missing = requiredPaths.filterNot { layout.projectDirectory.file(it).asFile.exists() }
        if (missing.isNotEmpty()) {
            throw GradleException(
                "Missing workspace paths: ${missing.joinToString()}. "
                    + "Run: ./gradlew syncSubmodules"
            )
        }
        if (!commandSucceeds("gpg", "--version")) {
            throw GradleException("Missing gpg. Maven publishing signs every artifact, including local .publishing/maven.")
        }
        val gpgSecretKeys = commandOutput("gpg", "--list-secret-keys", "--with-colons").orEmpty()
        val hasSecretKey = gpgSecretKeys.lineSequence().any { it.startsWith("sec:") || it.startsWith("sec#") }
        if (!hasSecretKey) {
            throw GradleException("No usable GPG secret key found. Create/import a signing key before publishing Maven artifacts.")
        }
    }
}

val prepareNowsWebDependencies by tasks.registering(Exec::class) {
    group = "nows"
    description = "Installs NowsWeb dependencies from package-lock.json."
    workingDir = nowsWebDir.asFile
    commandLine("npm", "ci")
    doFirst {
        if (!nowsWebDir.file("package.json").asFile.exists()) {
            throw GradleException(
                "Missing repos/NowsWeb/package.json. "
                    + "NowsWeb is optional/private; check it out separately before running buildNowsWeb."
            )
        }
        if (!commandSucceeds("npm", "--version")) {
            throw GradleException("Missing npm. It is required to build repos/NowsWeb.")
        }
    }
    inputs.files(
        nowsWebDir.file("package.json"),
        nowsWebDir.file("package-lock.json")
    )
    outputs.dir(nowsWebDir.dir("node_modules"))
}

val buildNowsWeb by tasks.registering(Exec::class) {
    group = "nows"
    description = "Builds the NowsWeb static site."
    dependsOn(prepareNowsWebDependencies)
    workingDir = nowsWebDir.asFile
    commandLine("npm", "run", "build")
    inputs.files(
        nowsWebDir.file("package.json"),
        nowsWebDir.file("package-lock.json"),
        nowsWebDir.file("tsconfig.json"),
        nowsWebDir.file("vite.config.ts"),
        nowsWebDir.file("index.html")
    )
    inputs.dir(nowsWebDir.dir("src"))
    outputs.dir(nowsWebDir.dir("dist"))
}

val syncSubmodules by tasks.registering(Exec::class) {
    group = "nows"
    description = "Initializes and updates required git submodules needed by the workspace."
    commandLine(
        "git",
        "submodule",
        "update",
        "--init",
        "--recursive",
        "repos/NowsInstaller",
        "repos/NowsGradlePlugin",
        "repos/NowsApiMod",
        "repos/NowsRemapper"
    )
    inputs.file(layout.projectDirectory.file(".gitmodules"))
}

checkWorkspacePrerequisites.configure {
    dependsOn(syncSubmodules)
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
    ":core" to "space/nows/nows-core/${project.version}/nows-core-${project.version}.jar",
    ":minecraft" to "space/nows/nows-minecraft/${project.version}/nows-minecraft-${project.version}.jar",
    ":mc:$minecraft" to "space/nows/nows-mc-$minecraft/${project.version}/nows-mc-$minecraft-${project.version}.jar",
    ":integrations:kdl" to "space/nows/nows-integration-kdl/${project.version}/nows-integration-kdl-${project.version}.jar",
    ":integrations:geb" to "space/nows/nows-integration-geb/${project.version}/nows-integration-geb-${project.version}.jar",
    ":integrations:logging" to "space/nows/nows-integration-logging/${project.version}/nows-integration-logging-${project.version}.jar",
    ":integrations:network" to "space/nows/nows-integration-network/${project.version}/nows-integration-network-${project.version}.jar",
    ":integrations:mixin" to "space/nows/nows-integration-mixin/${project.version}/nows-integration-mixin-${project.version}.jar",
    ":runtime" to "space/nows/nows-runtime/${project.version}/nows-runtime-${project.version}.jar"
)

fun publishedModuleArtifactIndex(projectPath: String, minecraft: String): Int? = when (projectPath) {
    ":core" -> 0
    ":minecraft" -> 1
    ":integrations:kdl" -> 2
    ":integrations:geb" -> 3
    ":integrations:logging" -> 4
    ":integrations:network" -> 19
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
    ":integrations:network" -> "nows-integration-network"
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
    "moshi-${providers.gradleProperty("moshi_version").get()}.jar" to "com/squareup/moshi/moshi/${providers.gradleProperty("moshi_version").get()}/moshi-${providers.gradleProperty("moshi_version").get()}.jar",
    "okio-jvm-${providers.gradleProperty("okio_version").get()}.jar" to "com/squareup/okio/okio-jvm/${providers.gradleProperty("okio_version").get()}/okio-jvm-${providers.gradleProperty("okio_version").get()}.jar",
    "kotlin-stdlib-${providers.gradleProperty("kotlin_version").get()}.jar" to "org/jetbrains/kotlin/kotlin-stdlib/${providers.gradleProperty("kotlin_version").get()}/kotlin-stdlib-${providers.gradleProperty("kotlin_version").get()}.jar",
    "kotlin-stdlib-jdk7-${providers.gradleProperty("kotlin_version").get()}.jar" to "org/jetbrains/kotlin/kotlin-stdlib-jdk7/${providers.gradleProperty("kotlin_version").get()}/kotlin-stdlib-jdk7-${providers.gradleProperty("kotlin_version").get()}.jar",
    "kotlin-stdlib-jdk8-${providers.gradleProperty("kotlin_version").get()}.jar" to "org/jetbrains/kotlin/kotlin-stdlib-jdk8/${providers.gradleProperty("kotlin_version").get()}/kotlin-stdlib-jdk8-${providers.gradleProperty("kotlin_version").get()}.jar",
    "annotations-${providers.gradleProperty("jetbrains_annotations_version").get()}.jar" to "org/jetbrains/annotations/${providers.gradleProperty("jetbrains_annotations_version").get()}/annotations-${providers.gradleProperty("jetbrains_annotations_version").get()}.jar",
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
        ":repos:NowsInstaller:allOfflineJars",
        ":repos:NowsGradlePlugin:jar",
        ":repos:NowsApiMod:allApiModJars"
    )

    inputs.file("repos/NowsInstaller/install.properties.template")
    inputs.property("nowsVersion", nowsVersion)
    inputs.property("minecraftVersion", minecraftVersion)
    inputs.property("publishedMinecraftVersions", publishedMinecraftVersions.joinToString(","))
    inputs.property("nowsReleaseBaseUrl", nowsReleaseBaseUrl)
    inputs.files(provider {
        publishedMinecraftVersions
            .flatMap { minecraft -> publishedModuleArtifactsFor(minecraft).keys }
            .distinct()
            .map { projectPath ->
                project(projectPath).tasks.named<Jar>("jar").get().archiveFile.get().asFile
            }
    })
    inputs.files(provider {
        val installerProject = project(":repos:NowsInstaller")
        listOf(
            installerProject.tasks.named<Jar>("jar").get().archiveFile.get().asFile,
            *publishedMinecraftVersions.map { minecraft ->
                installerProject.tasks.named<Jar>(offlineInstallerJarTaskName(minecraft)).get().archiveFile.get().asFile
            }.toTypedArray(),
            *publishedMinecraftVersions.map { minecraft ->
                project(":repos:NowsApiMod").tasks.named<Jar>(apiModJarTaskName(minecraft)).get().archiveFile.get().asFile
            }.toTypedArray(),
            project(":repos:NowsGradlePlugin").tasks.named<Jar>("jar").get().archiveFile.get().asFile
        )
    })
    inputs.files(provider {
        project(":repos:NowsInstaller").configurations.getByName("offlineMavenArtifacts").resolve()
    })
    outputs.dir(publishLayoutDir)

    doLast {
        val nows = nowsVersion.get()
        val installerProject = project(":repos:NowsInstaller")
        val offlineMavenArtifacts = installerProject.configurations.getByName("offlineMavenArtifacts").resolve()
        val installer = installerProject.tasks.named<Jar>("jar").get().archiveFile.get().asFile
        val offlineInstallers = publishedMinecraftVersions.associateWith { minecraft ->
            installerProject.tasks.named<Jar>(offlineInstallerJarTaskName(minecraft)).get().archiveFile.get().asFile
        }
        val apiMods = publishedMinecraftVersions.associateWith { minecraft ->
            project(":repos:NowsApiMod").tasks.named<Jar>(apiModJarTaskName(minecraft)).get().archiveFile.get().asFile
        }
        val gradlePlugin = project(":repos:NowsGradlePlugin").tasks.named<Jar>("jar").get().archiveFile.get().asFile
        val releaseVersionRoot = publishLayoutDir.dir("releases/nows/$nows").asFile
        val sharedInstallersRoot = releaseVersionRoot.resolve("installers")

        delete(releaseVersionRoot)
        sharedInstallersRoot.mkdirs()
        installer.copyTo(sharedInstallersRoot.resolve(installer.name), overwrite = true)
        offlineInstallers.values.forEach { offlineInstaller ->
            offlineInstaller.copyTo(sharedInstallersRoot.resolve(offlineInstaller.name), overwrite = true)
        }
        sharedInstallersRoot.resolve("SHA256SUMS").writeText(
            (listOf(installer) + offlineInstallers.values.sortedBy { it.name })
                .joinToString(System.lineSeparator(), postfix = System.lineSeparator()) {
                sha256(it) + "  " + it.name
            }
        )

        publishedMinecraftVersions.forEach { minecraft ->
            val releaseRoot = publishLayoutDir.dir("releases/nows/$nows/$minecraft").asFile
            val librariesRoot = releaseRoot.resolve("libraries")
            val modsRoot = releaseRoot.resolve("mods")
            val toolingRoot = releaseRoot.resolve("tooling")
            val artifactFiles = linkedMapOf<String, File>()

            delete(releaseRoot)
            librariesRoot.mkdirs()
            toolingRoot.mkdirs()

            val moduleArtifacts = publishedModuleArtifactsFor(minecraft)
            moduleArtifacts.forEach { (projectPath, relativePath) ->
                val source = project(projectPath).tasks.named<Jar>("jar").get().archiveFile.get().asFile
                val target = librariesRoot.resolve(relativePath)
                target.parentFile.mkdirs()
                source.copyTo(target, overwrite = true)
                artifactFiles[relativePath] = target
            }

            offlineMavenArtifacts.forEach { source ->
                val relativePath = publishedMavenArtifactPaths[source.name]
                    ?: throw GradleException("No publish path registered for ${source.name}")
                val target = librariesRoot.resolve(relativePath)
                target.parentFile.mkdirs()
                source.copyTo(target, overwrite = true)
                artifactFiles[relativePath] = target
            }

            modsRoot.mkdirs()
            val apiMod = apiMods[minecraft] ?: throw GradleException("No NowsApiMod jar registered for $minecraft")
            apiMod.copyTo(modsRoot.resolve(apiMod.name), overwrite = true)
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
                template["artifact.$index.coordinate"] = "space.nows:$artifactId:$nows"
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
}

tasks.register("publishMavenLayout") {
    group = "nows"
    description = "Publishes developer-facing Maven artifacts into .publishing/maven."
    dependsOn(mavenPublishedProjectPaths.map { "$it:publishAllPublicationsToPublishingMavenRepository" })
    dependsOn(":repos:NowsGradlePlugin:publishAllPublicationsToPublishingMavenRepository")
    dependsOn(":repos:NowsApiMod:publishApiModPublicationToPublishingMavenRepository")
}

tasks.register("prepareWorkspace") {
    group = "nows"
    description = "Fresh-clone setup: checks prerequisites, builds code/web artifacts and prepares release + Maven layouts."
    dependsOn(
        checkWorkspacePrerequisites,
        "versionReport",
        "dist",
        publishLayout
    )

    doLast {
        println("Nows workspace is prepared.")
        println("Release layout: ${publishLayoutDir.dir("releases").asFile}")
        println("Developer Maven layout: ${publishingMavenDir.asFile}")
        if (nowsWebDir.file("package.json").asFile.exists()) {
            println("Optional NowsWeb checkout: ${nowsWebDir.asFile}")
        }
    }
}

tasks.named("dist").configure { mustRunAfter(checkWorkspacePrerequisites) }
publishLayout.configure { mustRunAfter(checkWorkspacePrerequisites) }

gradle.projectsEvaluated {
    allprojects {
        tasks.configureEach {
            if (path != checkWorkspacePrerequisites.get().path && path != syncSubmodules.get().path) {
                mustRunAfter(checkWorkspacePrerequisites)
            }
        }
    }
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
