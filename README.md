# Kotlin owo extension

WIP extension for owo-lib config for Kotlin users
modifications in owo-lib are required for full functionality [PR here](https://github.com/wisp-forest/owo-lib/pull/121)


## Kotlin config setup:
Everything is the same as with Java [original wiki here](https://docs.wispforest.io/owo/config/getting-started/)  
**Except**   
Instead of the java annotationProcessor, you'll use this Kotlin Symbol Processor

```kotlin

plugins {
    id("fabric-loom") version "1.1-SNAPSHOT"
    kotlin("jvm") version "1.8.20"
    id("com.google.devtools.ksp") version "1.8.20-1.0.10"
    (...)
}
(...)

repositories {
    (...)
    maven("https://maven.wispforest.io")
    maven("https://maven.kosmx.dev/")
}

dependencies {
    (...)
    
    ksp("dev.kosmx.kowoconfig:ksp-owo-config:0.1.0") // Keep it updated
}
```
### Config model
```kotlin
@Config(name = "my-config", wrapperName = "MyConfig")
class MyConfigModel {
    @JvmField
    var anIntOption = 16
    @JvmField
    var aBooleanToggle = false
    @JvmField
    var anEnumOption = Choices.ANOTHER_CHOICE

    enum class Choices {
        A_CHOICE,
        ANOTHER_CHOICE
    }
}
```
`@JvmField` annotation is required until the PR will be merged.  

This config will generate **delegated properties** instead of java accessor methods.  
Keep that in mind when using the config: 

```kotlin
val CONFIG = MyConfig.createAndLoad()
    
fun someFun() {
    println(CONFIG.anIntOption) // get config value
    CONFIG.anIntOption = 42 // set config value
}
```

according to [Kotlin docs](https://kotlinlang.org/docs/java-to-kotlin-interop.html#properties), the accessor function names are `getAnIntOption` and `setAnIntOption`.  
*This is only needed when accessing the config from Java code*
