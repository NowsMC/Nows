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
    archiveBaseName.set("NowsInstaller")
    manifest { attributes("Main-Class" to application.mainClass.get()) }
}
