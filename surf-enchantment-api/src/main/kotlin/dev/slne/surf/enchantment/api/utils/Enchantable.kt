package dev.slne.surf.enchantment.api.utils

import dev.slne.surf.api.core.rarity.Rarity
import dev.slne.surf.api.paper.builder.LoreBuilder
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.Range

interface Enchantable {
    val key: Key
    val displayName: Component
    val description: LoreBuilder.(Int) -> Unit
    val rarity: Rarity
    val maxLevel: @Range(from = 1, to = 255) Int?

    fun buildLore(level: Int, displayDescription: Boolean = true) = LoreBuilder().apply {
        line {
            append(displayName.color(rarity.color))

            val max = maxLevel
            if (max == null || max > 1) {
                appendSpace()
                text(levelToRoman(level), rarity.color)
            }
        }

        if (displayDescription) {
            description(level)
        }
    }.build()

    private fun levelToRoman(level: Int): String {
        val numerals = listOf(
            1000 to "M",
            900 to "CM",
            500 to "D",
            400 to "CD",
            100 to "C",
            90 to "XC",
            50 to "L",
            40 to "XL",
            10 to "X",
            9 to "IX",
            5 to "V",
            4 to "IV",
            1 to "I"
        )

        var n = level
        val result = StringBuilder()

        for ((value, roman) in numerals) {
            val count = n / value
            repeat(count) { result.append(roman) }
            n %= value
        }

        return result.toString()
    }
}