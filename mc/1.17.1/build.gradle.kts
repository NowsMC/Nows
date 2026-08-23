plugins {
    `java-library`
    id("space.nows.gradle")
}

val javaRelease = 17

val minecraftVersion = project.name

nows {
    minecraftVersion.set(project.name)
    addMinecraftAdapter.set(false)
    addKdl.set(false)
    addGeb.set(false)
    addLogging.set(false)
    addNetwork.set(false)
    expandNowsModMetadata.set(false)
}

dependencies {
    api("com.squareup.moshi:moshi:${providers.gradleProperty("moshi_version").get()}")
    compileOnly("org.jspecify:jspecify:${providers.gradleProperty("jspecify_version").get()}")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaRelease)
}

tasks.jar {
    archiveBaseName.set("nows-mc-$minecraftVersion")
}
