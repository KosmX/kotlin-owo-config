package dev.kosmx.kowotest.config

import io.wispforest.owo.config.annotation.Config

/**
 * Example config from https://docs.wispforest.io/owo/config/getting-started/
 */

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