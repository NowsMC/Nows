plugins { `java-library` }

val minecraftVersion = project.name
val minecraftDevJar = rootProject.layout.projectDirectory.file(".nows/minecraft/$minecraftVersion/client-dev.jar")
val hasMinecraftDevJar = providers.provider { minecraftDevJar.asFile.isFile }

dependencies {
    compileOnly(project(":core"))
    compileOnly(files(minecraftDevJar))
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
