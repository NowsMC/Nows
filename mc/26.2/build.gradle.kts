plugins { `java-library` }

val minecraftVersion = project.name
val minecraftDevJar = rootProject.layout.projectDirectory.file(".nows/minecraft/$minecraftVersion/client-dev.jar")

dependencies {
    compileOnly(project(":core"))
    compileOnly(files(minecraftDevJar))
}

tasks.jar {
    archiveBaseName.set("nows-mc-$minecraftVersion")
}
