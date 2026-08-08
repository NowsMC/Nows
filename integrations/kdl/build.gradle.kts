plugins { `java-library` }

val kdl4jVersion = providers.gradleProperty("kdl4j_version")
dependencies {
    api(project(":core"))
    implementation("dev.kdl:kdl4j:${kdl4jVersion.get()}")
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar { archiveBaseName.set("nows-integration-kdl") }

tasks.test { useJUnitPlatform() }
