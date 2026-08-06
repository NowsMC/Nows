plugins { java }

val minecraftVersion = providers.gradleProperty("minecraft_version")
val officialMinecraftJar = rootProject.layout.projectDirectory.file(".nows/minecraft/${minecraftVersion.get()}/client-dev.jar")

dependencies {
    compileOnly(project(":core"))
    compileOnly("foo.zaaarf.geb:core:${providers.gradleProperty("geb_core_version").get()}")
    annotationProcessor("foo.zaaarf.geb:processor:${providers.gradleProperty("geb_processor_version").get()}")
    compileOnly(files(officialMinecraftJar))
    compileOnly("net.fabricmc:sponge-mixin:${providers.gradleProperty("mixin_version").get()}")
    annotationProcessor("net.fabricmc:sponge-mixin:${providers.gradleProperty("mixin_version").get()}")
}

tasks.compileJava {
    dependsOn(":minecraft:prepareMinecraft")
}

tasks.jar { archiveBaseName.set("nows-example-mod") }
