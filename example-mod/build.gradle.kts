plugins { java }

val minecraftVersion = providers.gradleProperty("minecraft_version")
val nowsVersion = providers.gradleProperty("nows_version")
val officialMinecraftJar = rootProject.layout.projectDirectory.file(".nows/minecraft/${minecraftVersion.get()}/client-dev.jar")

dependencies {
    compileOnly(project(":core"))
    compileOnly(project(":integrations:geb"))
    compileOnly(project(":integrations:logging"))
    compileOnly(project(":integrations:network"))
    compileOnly(project(":mc:${minecraftVersion.get()}"))
    annotationProcessor("foo.zaaarf.geb:processor:${providers.gradleProperty("geb_processor_version").get()}")
    compileOnly(files(officialMinecraftJar))
    compileOnly("org.spongepowered:mixin:${providers.gradleProperty("mixin_version").get()}")
    annotationProcessor("org.spongepowered:mixin:${providers.gradleProperty("mixin_version").get()}")
    annotationProcessor("com.google.code.gson:gson:2.14.0")
    annotationProcessor("com.google.guava:guava:33.4.8-jre")
    annotationProcessor("org.ow2.asm:asm:9.8")
    annotationProcessor("org.ow2.asm:asm-tree:9.8")
    annotationProcessor("org.ow2.asm:asm-commons:9.8")
    annotationProcessor("org.ow2.asm:asm-util:9.8")
}

tasks.compileJava {
    dependsOn(":minecraft:prepareMinecraft")
}

tasks.processResources {
    inputs.property("nowsVersion", nowsVersion)
    inputs.property("minecraftVersion", minecraftVersion)
    filesMatching("nows.mod.kdl") {
        expand(
            "nowsVersion" to nowsVersion.get(),
            "minecraftVersion" to minecraftVersion.get()
        )
    }
}

tasks.jar { archiveBaseName.set("nows-example-mod") }
