plugins {
    `java-gradle-plugin`
    `maven-publish`
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("org.ow2.asm:asm:9.8")
    implementation("org.ow2.asm:asm-commons:9.8")
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
