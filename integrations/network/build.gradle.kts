plugins { `java-library` }

dependencies {
    api(project(":core"))
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar { archiveBaseName.set("nows-integration-network") }

tasks.test { useJUnitPlatform() }
