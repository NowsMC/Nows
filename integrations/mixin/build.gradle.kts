plugins { `java-library` }

val mixinVersion = providers.gradleProperty("mixin_version")
val reactorVersion = providers.gradleProperty("reactor_version")

dependencies {
    api(project(":core"))
    implementation(project(":integrations:logging"))
    compileOnly("io.projectreactor:reactor-core:${reactorVersion.get()}")
    implementation("org.spongepowered:mixin:${mixinVersion.get()}")
    implementation("org.ow2.asm:asm:9.8")
    implementation("org.ow2.asm:asm-tree:9.8")
    implementation("org.ow2.asm:asm-commons:9.8")
    implementation("org.ow2.asm:asm-util:9.8")
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar { archiveBaseName.set("nows-integration-mixin") }

tasks.test { useJUnitPlatform() }
