@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.enchantments.beheading

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.api.core.rarity.Rarity
import dev.slne.surf.api.core.util.objectSetOf
import dev.slne.surf.enchantment.api.enchantment.AbstractCustomEnchantment
import dev.slne.surf.enchantment.api.enchantments.BeheadingEnchantment
import dev.slne.surf.enchantment.api.utils.CustomItemTypeTags
import dev.slne.surf.enchantment.paper.enchantments.beheading.listeners.BeheadingListener
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.keys.EnchantmentKeys
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import org.bukkit.inventory.EquipmentSlotGroup

@AutoService(BeheadingEnchantment::class)
class BeheadingEnchantmentImpl : AbstractCustomEnchantment(
    key = key("surf", "beheading"),
    displayName = text("Beheading"),
    rarity = Rarity.EPIC,
    description = { level ->
        line {
            val chance = level * CHANCE_PER_LEVEL
            darkSpacer("Gewährt eine")
            appendSpace()
            variableValue("$chance%")
            appendSpace()
            darkSpacer("Chance, den Kopf des Opfers fallen zu lassen")
        }
    },
    supportedItems = CustomItemTypeTags.BEHEADING_KEY.tagKey,
    weight = 1,
    minimumCost = EnchantmentRegistryEntry.EnchantmentCost.of(
        15,
        9
    ),
    maximumCost = EnchantmentRegistryEntry.EnchantmentCost.of(
        65,
        9
    ),

    activeSlots = setOf(EquipmentSlotGroup.MAINHAND),
    tags = objectSetOf(
        EnchantmentTagKeys.ON_RANDOM_LOOT,
        EnchantmentTagKeys.TREASURE
    ),
    exclusiveWith = setOf(EnchantmentKeys.LOOTING),
    maxLevel = 3,
    listeners = objectSetOf(BeheadingListener)
), BeheadingEnchantment {
    companion object {
        const val CHANCE_PER_LEVEL = 2
    }
}
