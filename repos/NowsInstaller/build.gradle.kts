plugins { application }

val kdl4jVersion = providers.gradleProperty("kdl4j_version")
val gebCoreVersion = providers.gradleProperty("geb_core_version")
val reactorVersion = providers.gradleProperty("reactor_version")
val disruptorVersion = providers.gradleProperty("disruptor_version")
val mixinVersion = providers.gradleProperty("mixin_version")

// GitHub Packages are resolved while building NowsInstaller, then embedded in the
// installer itself. End users therefore do not need GitHub credentials.
val embeddedGitHubPackages by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

val offlineMavenArtifacts by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

dependencies {
    embeddedGitHubPackages("dev.kdl:kdl4j:${kdl4jVersion.get()}")

    offlineMavenArtifacts("foo.zaaarf.geb:core:${gebCoreVersion.get()}")
    offlineMavenArtifacts("io.projectreactor:reactor-core:${reactorVersion.get()}")
    offlineMavenArtifacts("org.reactivestreams:reactive-streams:1.0.4")
    offlineMavenArtifacts("com.lmax:disruptor:${disruptorVersion.get()}")
    offlineMavenArtifacts("net.fabricmc:sponge-mixin:${mixinVersion.get()}")
    offlineMavenArtifacts("org.ow2.asm:asm:9.8")
    offlineMavenArtifacts("org.ow2.asm:asm-tree:9.8")
    offlineMavenArtifacts("org.ow2.asm:asm-commons:9.8")
    offlineMavenArtifacts("org.ow2.asm:asm-analysis:9.8")
    offlineMavenArtifacts("org.ow2.asm:asm-util:9.8")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

val embeddedDir = layout.buildDirectory.dir("generated/embedded-libs")
val prepareEmbeddedLibraries by tasks.registering(Copy::class) {
    from(embeddedGitHubPackages)
    into(embeddedDir)
}

val offlinePayloadDir = layout.buildDirectory.dir("generated/offline-installer")
val offlineModuleArtifacts = mapOf(
    ":core" to "space/nows/mcnows/nows-core/${project.version}/nows-core-${project.version}.jar",
    ":minecraft" to "space/nows/mcnows/nows-minecraft/${project.version}/nows-minecraft-${project.version}.jar",
    ":integrations:kdl" to "space/nows/mcnows/nows-integration-kdl/${project.version}/nows-integration-kdl-${project.version}.jar",
    ":integrations:geb" to "space/nows/mcnows/nows-integration-geb/${project.version}/nows-integration-geb-${project.version}.jar",
    ":integrations:logging" to "space/nows/mcnows/nows-integration-logging/${project.version}/nows-integration-logging-${project.version}.jar",
    ":integrations:mixin" to "space/nows/mcnows/nows-integration-mixin/${project.version}/nows-integration-mixin-${project.version}.jar",
    ":runtime" to "space/nows/mcnows/nows-runtime/${project.version}/nows-runtime-${project.version}.jar"
)
val offlineMavenArtifactPaths = mapOf(
    "core-${gebCoreVersion.get()}.jar" to "foo/zaaarf/geb/core/${gebCoreVersion.get()}/core-${gebCoreVersion.get()}.jar",
    "reactor-core-${reactorVersion.get()}.jar" to "io/projectreactor/reactor-core/${reactorVersion.get()}/reactor-core-${reactorVersion.get()}.jar",
    "reactive-streams-1.0.4.jar" to "org/reactivestreams/reactive-streams/1.0.4/reactive-streams-1.0.4.jar",
    "disruptor-${disruptorVersion.get()}.jar" to "com/lmax/disruptor/${disruptorVersion.get()}/disruptor-${disruptorVersion.get()}.jar",
    "sponge-mixin-${mixinVersion.get()}.jar" to "net/fabricmc/sponge-mixin/${mixinVersion.get()}/sponge-mixin-${mixinVersion.get()}.jar",
    "asm-9.8.jar" to "org/ow2/asm/asm/9.8/asm-9.8.jar",
    "asm-tree-9.8.jar" to "org/ow2/asm/asm-tree/9.8/asm-tree-9.8.jar",
    "asm-commons-9.8.jar" to "org/ow2/asm/asm-commons/9.8/asm-commons-9.8.jar",
    "asm-analysis-9.8.jar" to "org/ow2/asm/asm-analysis/9.8/asm-analysis-9.8.jar",
    "asm-util-9.8.jar" to "org/ow2/asm/asm-util/9.8/asm-util-9.8.jar"
)

val prepareOfflinePayload by tasks.registering(Copy::class) {
    dependsOn(offlineModuleArtifacts.keys.map { project(it).tasks.named("jar") })
    from("install.properties.template") {
        into("META-INF/nows/offline")
        rename { "install.properties" }
    }
    offlineModuleArtifacts.forEach { (projectPath, targetPath) ->
        val targetDir = targetPath.substringBeforeLast("/")
        val targetFile = targetPath.substringAfterLast("/")
        val jarFile = project(projectPath).tasks.named<Jar>("jar").flatMap { it.archiveFile }
        from(jarFile) {
            into("META-INF/nows/offline-libraries/$targetDir")
            rename { targetFile }
        }
    }
    from(offlineMavenArtifacts) {
        eachFile {
            val targetPath = offlineMavenArtifactPaths[name]
                ?: throw GradleException("No offline installer path registered for $name")
            path = "META-INF/nows/offline-libraries/$targetPath"
        }
        includeEmptyDirs = false
    }
    into(offlinePayloadDir)
}

tasks.processResources {
    dependsOn(prepareEmbeddedLibraries)
    from(embeddedDir) { into("META-INF/nows/embedded-libs") }
}

application { mainClass.set("space.nows.mcnows.installer.NowsInstaller") }

tasks.jar {
    dependsOn(prepareEmbeddedLibraries)
    archiveBaseName.set("NowsInstaller-cli")
    manifest { attributes("Main-Class" to application.mainClass.get()) }
}

val guiJar by tasks.registering(Jar::class) {
    group = "build"
    description = "Builds the Swing GUI Nows installer JAR."
    dependsOn(tasks.classes)
    archiveBaseName.set("NowsInstaller-ui")
    from(sourceSets.main.get().output)
    manifest { attributes("Main-Class" to "space.nows.mcnows.installer.NowsInstallerGui") }
}

val offlineJar by tasks.registering(Jar::class) {
    group = "build"
    description = "Builds the fully offline Nows installer JAR with all manifest artifacts embedded."
    dependsOn(tasks.classes, prepareOfflinePayload)
    archiveBaseName.set("NowsInstaller-offline")
    from(sourceSets.main.get().output)
    from(offlinePayloadDir)
    manifest { attributes("Main-Class" to "space.nows.mcnows.installer.NowsInstallerOffline") }
}

val devOfflineJar by tasks.registering(Jar::class) {
    group = "build"
    description = "Builds a development offline installer from this workspace's project JARs."
    dependsOn(tasks.classes, prepareOfflinePayload)
    archiveBaseName.set("NowsInstallerDevOffline")
    from(sourceSets.main.get().output)
    from(offlinePayloadDir)
    manifest { attributes("Main-Class" to "space.nows.mcnows.installer.NowsInstallerDevOffline") }
}

tasks.assemble {
    dependsOn(guiJar, offlineJar, devOfflineJar)
}
