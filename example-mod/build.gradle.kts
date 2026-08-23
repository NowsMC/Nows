plugins {
    id("space.nows.mcnows")
}

val configuredMinecraftVersion = providers.gradleProperty("minecraft_version")
val configuredNowsVersion = providers.gradleProperty("nows_version")

fun minecraftJavaRelease(minecraft: String): Int =
    if (minecraft.startsWith("26.") || minecraft in setOf("1.21.11", "1.21.1", "1.20.6")) 21 else 17

val javaRelease = minecraftJavaRelease(configuredMinecraftVersion.get())

nows {
    minecraftVersion.set(configuredMinecraftVersion)
    nowsVersion.set(configuredNowsVersion)
}

java {
    sourceCompatibility = JavaVersion.toVersion(javaRelease)
    targetCompatibility = JavaVersion.toVersion(javaRelease)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaRelease)
}

tasks.jar { archiveBaseName.set("nows-example-mod") }
