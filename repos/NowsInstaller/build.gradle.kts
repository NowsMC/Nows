plugins { application }

val kdl4jVersion = providers.gradleProperty("kdl4j_version")

// GitHub Packages are resolved while building NowsInstaller, then embedded in the
// installer itself. End users therefore do not need GitHub credentials.
val embeddedGitHubPackages by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

dependencies {
    embeddedGitHubPackages("dev.kdl:kdl4j:${kdl4jVersion.get()}")
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

tasks.assemble {
    dependsOn(guiJar)
}
