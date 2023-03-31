package dev.kosmx.kowotest

import dev.kosmx.kowotest.config.UwuConfig
import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory


val LOGGER: Logger = LoggerFactory.getLogger("uwu")

val config = UwuConfig.createAndLoad()

object ExampleMod : ModInitializer {
    override fun onInitialize() {
        LOGGER.info("Hello World!")

        config.thereAreStringsHere.let { println(it) }

        println(config.nestingTime.nestingTimeIntensifies.wowSoNested)

        println(config.aValue)
    }
}

