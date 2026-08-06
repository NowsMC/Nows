plugins { `java-library` }

val mixinVersion = providers.gradleProperty("mixin_version")
val reactorVersion = providers.gradleProperty("reactor_version")

dependencies {
    api(project(":core"))
    implementation(project(":integrations:logging"))
    compileOnly("io.projectreactor:reactor-core:${reactorVersion.get()}")
    implementation("net.fabricmc:sponge-mixin:${mixinVersion.get()}")
}

tasks.jar { archiveBaseName.set("nows-integration-mixin") }
