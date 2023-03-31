pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name= "Fabric"
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "kotlin-owo-config"

include(":testmod")
