plugins { `java-library` }

val kdl4jVersion = providers.gradleProperty("kdl4j_version")
val gebCoreVersion = providers.gradleProperty("geb_core_version")
val reactorVersion = providers.gradleProperty("reactor_version")
val disruptorVersion = providers.gradleProperty("disruptor_version")
val mixinVersion = providers.gradleProperty("mixin_version")

// This configuration is only for the optional future single-JAR distribution.
// It intentionally omits Minecraft-owned Log4j2/SLF4J/Gson/Guava/JSpecify.
val allJarLibraries by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation(project(":core"))
    implementation(project(":minecraft"))
    implementation(project(":integrations:kdl"))
    implementation(project(":integrations:geb"))
    implementation(project(":integrations:logging"))
    implementation(project(":integrations:mixin"))

    allJarLibraries("dev.kdl:kdl4j:${kdl4jVersion.get()}") { isTransitive = false }
    allJarLibraries("foo.zaaarf.geb:core:${gebCoreVersion.get()}") { isTransitive = false }
    allJarLibraries("io.projectreactor:reactor-core:${reactorVersion.get()}") { isTransitive = false }
    allJarLibraries("org.reactivestreams:reactive-streams:1.0.4") { isTransitive = false }
    allJarLibraries("com.lmax:disruptor:${disruptorVersion.get()}") { isTransitive = false }
    allJarLibraries("net.fabricmc:sponge-mixin:${mixinVersion.get()}") { isTransitive = false }
    allJarLibraries("org.ow2.asm:asm:9.8") { isTransitive = false }
    allJarLibraries("org.ow2.asm:asm-tree:9.8") { isTransitive = false }
    allJarLibraries("org.ow2.asm:asm-commons:9.8") { isTransitive = false }
    allJarLibraries("org.ow2.asm:asm-analysis:9.8") { isTransitive = false }
    allJarLibraries("org.ow2.asm:asm-util:9.8") { isTransitive = false }
}

tasks.jar {
    archiveBaseName.set("nows-runtime")
    manifest {
        attributes(
            "Main-Class" to "space.nows.mcnows.runtime.NowsLauncher",
            "Implementation-Title" to "Nows Runtime",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "nows.space"
        )
    }
}

// Optional monolithic distribution. Normal installation intentionally uses
// modular jars so volatile integrations can be replaced independently.
val internalModules = listOf(
    ":core",
    ":minecraft",
    ":integrations:kdl",
    ":integrations:geb",
    ":integrations:logging",
    ":integrations:mixin"
)

val allJar by tasks.registering(Jar::class) {
    archiveBaseName.set("nows")
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest { attributes("Main-Class" to "space.nows.mcnows.runtime.NowsLauncher") }

    dependsOn(internalModules.map { project(it).tasks.named("classes") })
    from(sourceSets.main.get().output)
    internalModules.forEach { path ->
        from(project(path).extensions.getByType<SourceSetContainer>().named("main").get().output)
    }
    from({ allJarLibraries.filter { it.name.endsWith(".jar") }.map { zipTree(it) } })

    exclude(
        "META-INF/*.SF",
        "META-INF/*.RSA",
        "META-INF/*.DSA",
        "module-info.class",
        "META-INF/versions/*/module-info.class"
    )
}
