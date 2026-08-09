plugins {
    `java-gradle-plugin`
    `maven-publish`
}

val nowsVersion = providers.gradleProperty("nows_version").orElse("development")

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("org.ow2.asm:asm:9.8")
    implementation("org.ow2.asm:asm-commons:9.8")
}

val pluginDefaultsDir = layout.buildDirectory.dir("generated/plugin-defaults")
val preparePluginDefaults by tasks.registering {
    inputs.property("nowsVersion", nowsVersion)
    outputs.dir(pluginDefaultsDir)

    doLast {
        val output = pluginDefaultsDir.get().file("defaults.properties").asFile
        output.parentFile.mkdirs()
        output.writeText("nows.version=${nowsVersion.get()}\n")
    }
}

tasks.processResources {
    dependsOn(preparePluginDefaults)
    from(pluginDefaultsDir) { into("META-INF/nows/gradle-plugin") }
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
