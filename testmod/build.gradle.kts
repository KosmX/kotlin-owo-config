plugins {
    id("fabric-loom") version "1.1-SNAPSHOT"
    kotlin("jvm") version "1.8.20"
    id("com.google.devtools.ksp") version "1.8.20-1.0.10"
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

version = "42.69"
group = (project.properties["maven_group"] as String) + ".testmod"

loom {
    runConfigs.configureEach {
        ideConfigGenerated(true)
    }
}

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com/releases/")
    mavenLocal()
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.properties["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${project.properties["yarn_mappings"]}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.properties["loader_version"]}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_version"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.properties["fabric_kotlin_version"]}")
    // Uncomment the following line to enable the deprecated Fabric API modules.
    // These are included in the Fabric API production distribution and allow you to update your mod to the latest modules at a later more convenient time.

    modImplementation("io.wispforest:owo-lib:${project.property("owo_version")}")

    modImplementation("com.terraformersmc:modmenu:${project.property("modmenu_version")}")

    ksp(rootProject)

}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    withType<JavaCompile> {
        options.release.set(java.targetCompatibility.majorVersion.toInt())
    }

    java {
        // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
        // if it is present.
        // If you remove this line, sources will not be generated.
        withSourcesJar()
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${base.archivesName.get()}" }
        }
    }
}

kotlin {
    jvmToolchain(java.targetCompatibility.majorVersion.toInt())
}
