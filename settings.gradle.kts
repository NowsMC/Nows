pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
    }
}

rootProject.name = "Nows"

include(
    "core",
    "minecraft",
    "integrations:kdl",
    "integrations:geb",
    "integrations:logging",
    "integrations:mixin",
    "runtime",
    "repos:NowsInstaller",
    "repos:NowsGradlePlugin",
    "example-mod"
)
