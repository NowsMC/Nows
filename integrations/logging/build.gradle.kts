plugins { `java-library` }

val reactorVersion = providers.gradleProperty("reactor_version")
val disruptorVersion = providers.gradleProperty("disruptor_version")

dependencies {
    api(project(":core"))
    api("io.projectreactor:reactor-core:${reactorVersion.get()}")
    implementation("com.lmax:disruptor:${disruptorVersion.get()}")
    compileOnly("org.slf4j:slf4j-api:2.0.17")
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar { archiveBaseName.set("nows-integration-logging") }

tasks.test { useJUnitPlatform() }
