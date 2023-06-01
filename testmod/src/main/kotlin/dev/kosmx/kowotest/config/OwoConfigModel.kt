package dev.kosmx.kowotest.config

import blue.endless.jankson.Comment
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.annotation.*
import io.wispforest.owo.ui.core.Color

@Modmenu(modId = "uwu")
@Config(name = "uwu", wrapperName = "UwuConfig")
class UwuConfigModel {
    @SectionHeader("top")
    @RangeConstraint(min = 0.0, max = 56.0)
    @JvmField
    var aValue = 56

    @RegexConstraint("[A-Za-z]{1,3}")
    @JvmField
    var regex = "yes"

    @Nest
    @Expanded
    @SectionHeader("nesting_yo?")
    @JvmField
    var nestingTime = Nested()

    //@PredicateConstraint("predicateFunction") predicate doesn't work without modification in OwoLib
    @JvmField
    var someOption: List<String> = ArrayList(listOf("1", "2", "3", "4", "5"))

    @RangeConstraint(min = 0.0, max = 10.0, decimalPlaces = 1)
    @JvmField
    var floting = 6.9f

    @JvmField
    var thisIsAStringValue = "\\bruh?"

    @SectionHeader("bottom")
    @JvmField
    var thereAreStringsHere: List<String> = ArrayList(listOf("yes", "no"))

    @RestartRequired
    @JvmField
    var broTheresAnEnum = WowValues.FIRST

    @Hook
    @JvmField
    var anEpicColor = Color.BLUE

    @WithAlpha
    @JvmField
    var anEpicColorWithAlpha = Color.GREEN

    @ExcludeFromScreen
    @JvmField
    var noSeeingThis = "yep, never"

    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    @Nest
    @JvmField
    var nestParty = Nested()

    class Nested {
        @JvmField
        var togglee = false

        @JvmField
        var yesThisIsAlsoNested = true

        @Nest
        @Comment("Commented nesting")
        @JvmField
        var nestingTimeIntensifies = SuperNested()

        @Sync(Option.SyncMode.INFORM_SERVER)
        @JvmField
        var nestedIntegers: List<Int> = ArrayList(listOf(69, 34, 35, 420))
    }


    class SuperNested {
        @JvmField
        var wowSoNested: Byte = 0
    }


    enum class WowValues {
        FIRST,
        SECOND,
        THIRD,
        FOURTH
    }


    // so we declare a predicate method
    fun predicateFunction(list: List<String?>): Boolean {
        // and do the check in here
        // this could be arbitrarily complex code, but
        // we'll keep it simple for this demonstration
        return list.size == 5
    }

}