@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.enchantments.rocketsaver

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.api.core.rarity.Rarity
import dev.slne.surf.enchantment.api.enchantment.AbstractCustomEnchantment
import dev.slne.surf.enchantment.api.enchantments.RocketSaverEnchantment
import dev.slne.surf.enchantment.api.utils.CustomItemTypeTags
import dev.slne.surf.enchantment.paper.enchantments.beheading.BeheadingEnchantmentImpl.Companion.CHANCE_PER_LEVEL
import dev.slne.surf.enchantment.paper.enchantments.rocketsaver.listeners.RocketSaverListener
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.inventory.EquipmentSlotGroup

@AutoService(RocketSaverEnchantment::class)
class RocketSaverEnchantmentImpl : AbstractCustomEnchantment(
    key = key("surf", "rocket_saver"),
    displayName = text("Rocket Saver"),
    rarity = Rarity.MYTHIC,
    description = { level ->
        line {
            val chance = level * CHANCE_PER_LEVEL
            darkSpacer("Gewährt eine")
            appendSpace()
            variableValue("$chance%")
            appendSpace()
            darkSpacer("Chance, Beim Boost")
        }
        line {
            darkSpacer("keine Feuerwerkskörper zu verbrauchen")
        }
    },
    supportedItems = CustomItemTypeTags.ROCKET_SAVER_KEY.tagKey,
    weight = 2,
    minimumCost = EnchantmentRegistryEntry.EnchantmentCost.of(
        15,
        9
    ),
    maximumCost = EnchantmentRegistryEntry.EnchantmentCost.of(
        65,
        9
    ),
    activeSlots = setOf(EquipmentSlotGroup.CHEST),
    maxLevel = 3,
    obtainableFromGameplay = false,
    listeners = setOf(RocketSaverListener)
), RocketSaverEnchantment {
    companion object {
        const val CHANCE_PER_LEVEL = 15
    }
}