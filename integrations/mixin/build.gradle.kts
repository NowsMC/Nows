plugins { `java-library` }

val mixinVersion = providers.gradleProperty("mixin_version")
dependencies {
    api(project(":core"))
    implementation(project(":integrations:logging"))
    implementation("net.fabricmc:sponge-mixin:${mixinVersion.get()}")
}

tasks.jar { archiveBaseName.set("nows-integration-mixin") }
