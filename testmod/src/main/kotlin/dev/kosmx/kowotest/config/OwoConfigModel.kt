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
    val aValue = 56

    @RegexConstraint("[A-Za-z]{1,3}")
    val regex = "yes"

    @Nest
    @Expanded
    @SectionHeader("nesting_yo?")
    var nestingTime = Nested()

    @PredicateConstraint("predicateFunction")
    var someOption: List<String> = ArrayList(listOf("1", "2", "3", "4", "5"))

    @RangeConstraint(min = 0.0, max = 10.0, decimalPlaces = 1)
    var floting = 6.9f

    var thisIsAStringValue = "\\bruh?"

    @SectionHeader("bottom")
    var thereAreStringsHere: List<String> = ArrayList(listOf("yes", "no"))

    @RestartRequired
    var broTheresAnEnum = WowValues.FIRST

    @Hook
    var anEpicColor = Color.BLUE

    @WithAlpha
    var anEpicColorWithAlpha = Color.GREEN

    @ExcludeFromScreen
    var noSeeingThis = "yep, never"

    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    @Nest
    var nestParty = Nested()

    class Nested {
        var togglee = false
        var yesThisIsAlsoNested = true

        @Nest
        @Comment("Commented nesting")
        var nestingTimeIntensifies = SuperNested()

        @Sync(Option.SyncMode.INFORM_SERVER)
        var nestedIntegers: List<Int> = ArrayList(listOf(69, 34, 35, 420))
    }


    class SuperNested {
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