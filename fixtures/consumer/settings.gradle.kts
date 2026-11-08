pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
    includeBuild("../../repos/NowsGradlePlugin")
}

rootProject.name = "nows_consumer_fixture"

// Resolve the Nows artifacts that the plugin adds by coordinate from the working tree, without publishing.
includeBuild("../..") {
    dependencySubstitution {
        val nowsVersion = providers.gradleProperty("nows_version").get()
        fun substituteNows(artifactId: String, projectPath: String) {
            substitute(module("space.nows:$artifactId:$nowsVersion")).using(project(projectPath))
            substitute(module("space.nows:$artifactId")).using(project(projectPath))
        }
        substituteNows("nows-core", ":core")
        substituteNows("nows-integration-kdl", ":integrations:kdl")
        substituteNows("nows-integration-geb", ":integrations:geb")
        substituteNows("nows-integration-logging", ":integrations:logging")
        substituteNows("nows-integration-network", ":integrations:network")
        substituteNows("nows-integration-mixin", ":integrations:mixin")
        listOf("26.2", "26.1.2", "1.21.11", "1.21.1", "1.20.6", "1.20.1", "1.19.4", "1.19.2", "1.18.2", "1.17.1", "1.16.5", "1.16.4")
            .forEach { substituteNows("nows-mc-$it", ":mc:$it") }
    }
}
