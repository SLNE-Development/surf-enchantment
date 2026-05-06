@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.enchantments.rocketride

import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.api.core.rarity.Rarity
import dev.slne.surf.api.core.util.objectSetOf
import dev.slne.surf.enchantment.api.enchantment.AbstractCustomEnchantment
import dev.slne.surf.enchantment.api.enchantments.RocketRideEnchantment
import dev.slne.surf.enchantment.api.utils.CustomItemTypeTags
import dev.slne.surf.enchantment.paper.enchantments.lumberjack.LumberjackEnchantmentImpl
import dev.slne.surf.enchantment.paper.enchantments.rocketride.listeners.RocketRideBoostListener
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys

//@AutoService(RocketRideEnchantment::class)
class RocketRideEnchantmentImpl : AbstractCustomEnchantment(
    key = key("surf", "rocket_ride"),
    displayName = text("Rocket Ride"),
    rarity = Rarity.EPIC,
    description = {
        line {
            darkSpacer("Boostet Happy Ghasts je nach Rakete mit")
            appendSpace()
            variableValue("${MIN_MULTIPLIER}x")
            appendSpace()
            darkSpacer("bis")
            appendSpace()
            variableValue("${MAX_MULTIPLIER}x")
            appendSpace()
            darkSpacer("Stärke")
        }
        line {
            darkSpacer("Boostdauer:")
            appendSpace()
            variableValue("${durationSecondsForLevel(1)}")
            appendSpace()
            darkSpacer("bis")
            appendSpace()
            variableValue("${durationSecondsForLevel(MAX_LEVEL)}")
            appendSpace()
            darkSpacer("Sekunden")        }
    },
    supportedItems = CustomItemTypeTags.ROCKET_RIDE_KEY.tagKey,
    weight = 2,
    minimumCost = EnchantmentRegistryEntry.EnchantmentCost.of(
        15,
        9
    ),
    maximumCost = EnchantmentRegistryEntry.EnchantmentCost.of(
        65,
        9
    ),
    tags = objectSetOf(
        EnchantmentTagKeys.ON_RANDOM_LOOT,
        EnchantmentTagKeys.TREASURE
    ),
    maxLevel = MAX_LEVEL,
    listeners = objectSetOf(RocketRideBoostListener),
    jobs = objectSetOf(RocketRideBoostListener.cooldownHandler)
), RocketRideEnchantment {
    companion object {
        const val MAX_LEVEL = 3
        const val BASE_POWER = 0.9
        val ROCKET_PROPERTIES = mapOf(
            1 to RocketBoost(1.4, 0.5, 5),
            2 to RocketBoost(1.9, 0.7, 10),
            3 to RocketBoost(2.6, 0.9, 15)
        )

        val MIN_MULTIPLIER = ROCKET_PROPERTIES.getValue(1).multiplier
        val MAX_MULTIPLIER = ROCKET_PROPERTIES.getValue(MAX_LEVEL).multiplier

        fun boostForLevel(level: Int) = ROCKET_PROPERTIES[level] ?: ROCKET_PROPERTIES[1]!!
        fun durationTicksForLevel(level: Int) = 20 + level.coerceIn(1, MAX_LEVEL) * 10
        fun durationSecondsForLevel(level: Int) = durationTicksForLevel(level) / 20.0
    }
}
