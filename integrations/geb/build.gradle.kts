plugins { `java-library` }

val gebCoreVersion = providers.gradleProperty("geb_core_version")
val gebProcessorVersion = providers.gradleProperty("geb_processor_version")

dependencies {
    api(project(":core"))
    api("foo.zaaarf.geb:core:${gebCoreVersion.get()}")
    implementation("foo.zaaarf.geb:processor:${gebProcessorVersion.get()}")
    annotationProcessor("foo.zaaarf.geb:processor:${gebProcessorVersion.get()}")
}

tasks.jar { archiveBaseName.set("nows-integration-geb") }