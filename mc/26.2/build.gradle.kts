plugins { `java-library` }

val minecraftVersion = project.name
val minecraftDevJar = rootProject.layout.projectDirectory.file(".nows/minecraft/$minecraftVersion/client-dev.jar")

dependencies {
    compileOnly(project(":core"))
    compileOnly(files(minecraftDevJar))
    compileOnly("com.mojang:datafixerupper:${providers.gradleProperty("datafixerupper_version").get()}")
    compileOnly("org.spongepowered:mixin:${providers.gradleProperty("mixin_version").get()}")
}

tasks.compileJava {
    dependsOn(":minecraft:prepareMinecraft")
}

tasks.jar {
    archiveBaseName.set("nows-mc-$minecraftVersion")
}
