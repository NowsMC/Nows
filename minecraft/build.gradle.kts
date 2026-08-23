plugins { `java-library` }

val minecraftVersion = providers.gradleProperty("minecraft_version")
val monorepoDevJar = rootProject.layout.projectDirectory.file(".nows/minecraft/${minecraftVersion.get()}/client-dev.jar")

dependencies {
    api(project(":core"))
    implementation("com.google.code.gson:gson:2.14.0")
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar { archiveBaseName.set("nows-minecraft") }

tasks.test { useJUnitPlatform() }

tasks.processResources {
    from(rootProject.layout.projectDirectory.dir("mc")) {
        into("META-INF/nows/mc")
        include("*/nows-minecraft.properties")
    }
}

val prepareMinecraft by tasks.registering(JavaExec::class) {
    group = "nows"
    description = "Prepares the Mojang-named Minecraft JAR used by this monorepo."
    dependsOn(tasks.classes)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("space.nows.minecraft.PrepareMinecraftCli")
    args(minecraftVersion.get(), monorepoDevJar.asFile.absolutePath)
    outputs.file(monorepoDevJar)
}
