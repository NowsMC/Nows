plugins {
    `java-gradle-plugin`
    `maven-publish`
}

val tinyRemapperVersion = providers.gradleProperty("tiny_remapper_version")
val mappingIoVersion = providers.gradleProperty("mapping_io_version")

dependencies {
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("net.fabricmc:tiny-remapper:${tinyRemapperVersion.get()}")
    implementation("net.fabricmc:mapping-io:${mappingIoVersion.get()}")
}

gradlePlugin {
    plugins {
        create("nows") {
            id = "space.nows.mcnows"
            implementationClass = "space.nows.mcnows.gradle.NowsGradlePlugin"
            displayName = "Nows Gradle Plugin"
            description = "Official Mojang mappings, Minecraft dev classpath and Nows mod tooling."
        }
    }
}

tasks.jar { archiveBaseName.set("NowsGradlePlugin") }
