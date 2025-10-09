import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    kotlin("jvm")
    `maven-publish`
}
val archivesBaseName = providers.gradleProperty("archives_base_name")
val modVersion = providers.gradleProperty("mod_version")
val mavenGroup = providers.gradleProperty("maven_group")
val owoVersion = providers.gradleProperty("owo_version")
val kspVersion = providers.gradleProperty("ksp_version")
val kotlinPoetVersion = providers.gradleProperty("kotlin_poet_version")
val javaVersion = providers.gradleProperty("java_version")
base.archivesName = archivesBaseName.get()
version = modVersion.get()
group = mavenGroup.get()
repositories {
    mavenCentral()
    maven ("https://maven.fabricmc.net/") { name = "Fabric" }
    maven("https://maven.wispforest.io")
}
dependencies {
    implementation("io.wispforest:owo-lib:${owoVersion.get()}")
    implementation("com.google.devtools.ksp:symbol-processing-api:${kspVersion.get()}")
    implementation("com.squareup:kotlinpoet-ksp:${kotlinPoetVersion.get()}")
}
tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.get()
        targetCompatibility = javaVersion.get()
        options.release = javaVersion.get().toInt()
    }
    withType<JavaExec>().configureEach { defaultCharacterEncoding = "UTF-8" }
    withType<Javadoc>().configureEach { options.encoding = "UTF-8" }
    withType<Test>().configureEach { defaultCharacterEncoding = "UTF-8" }
    withType<KotlinCompile>().configureEach {
        compilerOptions {
            extraWarnings = true
            jvmTarget = JvmTarget.valueOf("JVM_${javaVersion.get()}")
        }
    }
    java {
        toolchain.languageVersion = JavaLanguageVersion.of(javaVersion.get())
        sourceCompatibility = JavaVersion.toVersion(javaVersion.get().toInt())
        targetCompatibility = JavaVersion.toVersion(javaVersion.get().toInt())
        withSourcesJar()
    }
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
