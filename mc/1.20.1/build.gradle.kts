plugins { `java-library` }

val minecraftVersion = project.name
val minecraftDevJar = rootProject.layout.projectDirectory.file(".nows/minecraft/$minecraftVersion/client-dev.jar")
val hasMinecraftDevJar = providers.provider { minecraftDevJar.asFile.isFile }

dependencies {
    compileOnly(project(":core"))
    compileOnly(files(minecraftDevJar))
    compileOnly("com.mojang:datafixerupper:${providers.gradleProperty("datafixerupper_version").get()}")
    compileOnly("com.mojang:brigadier:${providers.gradleProperty("brigadier_version").get()}")
    api("com.squareup.moshi:moshi:${providers.gradleProperty("moshi_version").get()}")
    compileOnly("org.spongepowered:mixin:${providers.gradleProperty("mixin_version").get()}")
}

tasks.withType<JavaCompile>().configureEach {
    onlyIf {
        if (!hasMinecraftDevJar.get()) {
            logger.lifecycle("Skipping {} because {} is missing. Prepare a remapped Minecraft {} dev jar first.",
                path, minecraftDevJar.asFile, minecraftVersion)
            false
        } else {
            true
        }
    }
}

tasks.jar {
    archiveBaseName.set("nows-mc-$minecraftVersion")
    onlyIf { hasMinecraftDevJar.get() }
}
