@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.enchantments.experience

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.api.core.rarity.Rarity
import dev.slne.surf.enchantment.api.enchantment.AbstractCustomEnchantment
import dev.slne.surf.enchantment.api.enchantments.ExperienceEnchantment
import dev.slne.surf.enchantment.api.utils.CustomItemTypeTags
import dev.slne.surf.enchantment.paper.enchantments.experience.listeners.ExperienceListener
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.keys.EnchantmentKeys
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import org.bukkit.inventory.EquipmentSlotGroup

@AutoService(ExperienceEnchantment::class)
class ExperienceEnchantmentImpl : AbstractCustomEnchantment(
    key = key("surf", "experience"),
    displayName = text("Experience"),
    rarity = Rarity.EPIC,
    description = { level ->
        line {
            val bonus = level * XP_BONUS_PERCENT_PER_LEVEL
            darkSpacer("Erhöht gedroppte Erfahrung um")
            appendSpace()
            variableValue("$bonus%")
        }
    },
    supportedItems = CustomItemTypeTags.EXPERIENCE_KEY.tagKey,
    weight = 1,
    minimumCost = EnchantmentRegistryEntry.EnchantmentCost.of(
        15,
        9
    ),
    maximumCost = EnchantmentRegistryEntry.EnchantmentCost.of(
        65,
        9
    ),

    tags = setOf(
        EnchantmentTagKeys.ON_RANDOM_LOOT,
        EnchantmentTagKeys.TREASURE
    ),
    exclusiveWith = setOf(EnchantmentKeys.LOOTING, EnchantmentKeys.SILK_TOUCH),
    activeSlots = setOf(EquipmentSlotGroup.HAND),
    maxLevel = 3,
    listeners = setOf(ExperienceListener)
), ExperienceEnchantment {
    companion object {
        const val XP_BONUS_PERCENT_PER_LEVEL = 10
    }
}
