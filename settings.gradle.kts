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
    "mc:26.2",
    "mc:1.20.1",
    "integrations:kdl",
    "integrations:geb",
    "integrations:logging",
    "integrations:mixin",
    "runtime",
    "repos:NowsInstaller",
    "repos:NowsGradlePlugin",
    "example-mod"
)
