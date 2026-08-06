plugins { `java-library` }

val reactorVersion = providers.gradleProperty("reactor_version")
val disruptorVersion = providers.gradleProperty("disruptor_version")

dependencies {
    api(project(":core"))
    api("io.projectreactor:reactor-core:${reactorVersion.get()}")
    implementation("com.lmax:disruptor:${disruptorVersion.get()}")
    compileOnly("org.slf4j:slf4j-api:2.0.17")
}

tasks.jar { archiveBaseName.set("nows-integration-logging") }