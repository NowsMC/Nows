plugins { base }

val nowsVersion = providers.gradleProperty("nows_version")

allprojects {
    group = "space.nows.mcnows"
    version = nowsVersion.get()

    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")

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
                password = providers.gradleProperty("gpr.key")
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
        ":integrations:kdl:jar",
        ":integrations:geb:jar",
        ":integrations:logging:jar",
        ":integrations:mixin:jar",
        ":runtime:jar",
        ":repos:NowsInstaller:jar",
        ":repos:NowsGradlePlugin:jar",
        ":example-mod:jar"
    )
}

tasks.register("allJar") {
    group = "nows"
    description = "Builds the optional single-JAR Nows distribution. Modular install remains the default."
    dependsOn(":runtime:allJar")
}
