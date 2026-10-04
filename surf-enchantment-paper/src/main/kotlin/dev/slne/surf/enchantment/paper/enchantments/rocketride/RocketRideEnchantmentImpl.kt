@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.enchantments.rocketride

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.api.core.rarity.Rarity
import dev.slne.surf.api.core.util.objectSetOf
import dev.slne.surf.enchantment.api.enchantment.AbstractCustomEnchantment
import dev.slne.surf.enchantment.api.enchantments.RocketRideEnchantment
import dev.slne.surf.enchantment.api.utils.CustomItemTypeTags
import dev.slne.surf.enchantment.paper.enchantments.rocketride.listeners.RocketRideBoostListener
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import kotlin.time.Duration.Companion.seconds

@AutoService(RocketRideEnchantment::class)
class RocketRideEnchantmentImpl : AbstractCustomEnchantment(
    key = key("surf", "rocket_ride_v1"),
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
            darkSpacer("Cooldown:")
            appendSpace()
            variableValue("${cooldownForLevel(1).inWholeSeconds}")
            appendSpace()
            darkSpacer("bis")
            appendSpace()
            variableValue("${cooldownForLevel(MAX_LEVEL).inWholeSeconds}")
            appendSpace()
            darkSpacer("Sekunden (je nach Stufe)")
        }
        line {
            darkSpacer("Boostdauer:")
            appendSpace()
            variableValue("${durationSecondsForRocketTier(1)}")
            appendSpace()
            darkSpacer("bis")
            appendSpace()
            variableValue("${durationSecondsForRocketTier(MAX_ROCKET_TIER)}")
            appendSpace()
            darkSpacer("Sekunden")
        }
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
        const val MAX_ROCKET_TIER = 3
        const val BASE_POWER = 0.9
        val ROCKET_PROPERTIES = mapOf(
            1 to RocketBoost(1.4, 0.5),
            2 to RocketBoost(1.9, 0.7),
            3 to RocketBoost(2.6, 0.9)
        )

        val MIN_MULTIPLIER = ROCKET_PROPERTIES.getValue(1).multiplier
        val MAX_MULTIPLIER = ROCKET_PROPERTIES.getValue(MAX_ROCKET_TIER).multiplier

        private val LEVEL_COOLDOWNS = mapOf(
            1 to 30.seconds,
            2 to 20.seconds,
            3 to 10.seconds
        )

        fun boostForRocketTier(tier: Int) = ROCKET_PROPERTIES[tier] ?: ROCKET_PROPERTIES[1]!!
        fun durationTicksForRocketTier(tier: Int) = 20 + tier.coerceIn(1, MAX_ROCKET_TIER) * 10
        fun durationSecondsForRocketTier(tier: Int) = durationTicksForRocketTier(tier) / 20.0
        fun cooldownForLevel(level: Int) = LEVEL_COOLDOWNS.getValue(level.coerceIn(1, MAX_LEVEL))
    }
}
