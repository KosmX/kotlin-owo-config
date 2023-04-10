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

// To use the testmod module, create a file "test" in the project directory  ;)
if (File("test").isFile) {
    include(":testmod")
}
