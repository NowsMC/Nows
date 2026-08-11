plugins { `java-library` }

val kdl4jVersion = providers.gradleProperty("kdl4j_version")
val kdl4jCoordinate = "com.github.kdl-org:kdl4j:v${kdl4jVersion.get()}"
dependencies {
    api(project(":core"))
    implementation(kdl4jCoordinate)
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar { archiveBaseName.set("nows-integration-kdl") }

tasks.test { useJUnitPlatform() }
