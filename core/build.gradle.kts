plugins { `java-library` }

tasks.jar {
    archiveBaseName.set("nows-core")
    manifest {
        attributes(
            "Implementation-Title" to "Nows Core",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "nows.space"
        )
    }
}

tasks.test { useJUnitPlatform() }

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
