plugins { `java-library` }

val kdl4jVersion = providers.gradleProperty("kdl4j_version")
dependencies {
    api(project(":core"))
    implementation("dev.kdl:kdl4j:${kdl4jVersion.get()}")
}

tasks.jar { archiveBaseName.set("nows-integration-kdl") }
