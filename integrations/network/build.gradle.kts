plugins { `java-library` }

val nettyVersion = providers.gradleProperty("netty_version")

dependencies {
    api(project(":core"))
    compileOnlyApi("io.netty:netty-buffer:${nettyVersion.get()}")
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.netty:netty-buffer:${nettyVersion.get()}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar { archiveBaseName.set("nows-integration-network") }

tasks.test { useJUnitPlatform() }
