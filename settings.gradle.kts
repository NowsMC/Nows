pluginManagement {
    includeBuild("repos/NowsGradlePlugin")
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
}

rootProject.name = "Nows"

include(
    "core",
    "minecraft",
    "mc:26.2",
    "mc:26.1.2",
    "mc:1.21.11",
    "mc:1.21.1",
    "mc:1.20.6",
    "mc:1.20.1",
    "mc:1.19.4",
    "mc:1.19.2",
    "mc:1.18.2",
    "mc:1.17.1",
    "mc:1.16.5",
    "mc:1.16.4",
    "integrations:kdl",
    "integrations:geb",
    "integrations:logging",
    "integrations:network",
    "integrations:mixin",
    "runtime",
    "repos:NowsRemapper",
    "repos:NowsInstaller",
    "repos:NowsGradlePlugin",
    "repos:NowsApiMod",
    "example-mod"
)
