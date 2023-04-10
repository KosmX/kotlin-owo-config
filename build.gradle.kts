plugins {
    kotlin("jvm") version "1.8.20"
    `maven-publish`
}

group = project.property("maven_group") as String
version = project.property("version") as String

java.targetCompatibility = JavaVersion.VERSION_17
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()

    maven ("https://maven.fabricmc.net/") { name = "Fabric" }
    maven("https://maven.wispforest.io")
}

dependencies {

    implementation("io.wispforest:owo-lib:0.10.3+1.19.3")
    //compileOnly("blue.endless:jankson:1.2.2")

    /// KSP deps
    implementation("com.google.devtools.ksp:symbol-processing-api:1.8.20-1.0.10")
    implementation("com.squareup:kotlinpoet-ksp:1.12.0")
}


tasks {
    withType<JavaCompile> {
        options.release.set(java.targetCompatibility.majorVersion.toInt())
    }

    java {
        withSourcesJar()
    }
}

kotlin {
    jvmToolchain(java.targetCompatibility.majorVersion.toInt())
}

val env: Map<String, String> = System.getenv()

publishing {
    publications {
        create<MavenPublication>("ksp_stuff") {
            artifactId = "ksp-owo-config"
            from(components["java"])
        }
    }
    repositories {
        if ("MAVEN_USER" in env) {
            maven("https://maven.kosmx.dev/") {
                credentials {
                    username = env["MAVEN_USER"]
                    password = env["MAVEN_PASS"]
                }
            }
        } else {
            mavenLocal()
        }
    }
}
