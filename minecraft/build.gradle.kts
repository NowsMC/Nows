plugins { `java-library` }

val minecraftVersion = providers.gradleProperty("minecraft_version")
val monorepoDevJar = rootProject.layout.projectDirectory.file(".nows/minecraft/${minecraftVersion.get()}/client-dev.jar")

dependencies {
    api(project(":core"))
    implementation("com.google.code.gson:gson:2.14.0")
}

tasks.jar { archiveBaseName.set("nows-minecraft") }

tasks.processResources {
    from(rootProject.layout.projectDirectory.dir("mc")) {
        into("META-INF/nows/mc")
        include("*/nows-minecraft.properties")
    }
}

val prepareMinecraft by tasks.registering(JavaExec::class) {
    group = "nows"
    description = "Prepares the Mojang-named Minecraft JAR used by this monorepo's example mod."
    dependsOn(tasks.classes)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("space.nows.mcnows.minecraft.PrepareMinecraftCli")
    args(minecraftVersion.get(), monorepoDevJar.asFile.absolutePath)
    outputs.file(monorepoDevJar)
}
