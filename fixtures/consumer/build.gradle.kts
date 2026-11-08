plugins {
    id("space.nows.gradle")
}

group = "space.nows.fixture"
version = providers.gradleProperty("mod_version").get()

nows {
    minecraftVersion.set(providers.gradleProperty("minecraft_version"))
    nowsVersion.set(providers.gradleProperty("nows_version"))
    addMixin.set(false)
}

sourceSets.main {
    java.srcDir("src/verification/java")
}
