package dev.slne.surf.enchantment.paper.enchantments.throwback

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.api.core.rarity.Rarity
import dev.slne.surf.api.core.util.objectSetOf
import dev.slne.surf.enchantment.api.enchantment.AbstractCustomEnchantment
import dev.slne.surf.enchantment.api.enchantments.ThrowBackEnchantment
import dev.slne.surf.enchantment.api.utils.CustomItemTypeTags
import dev.slne.surf.enchantment.paper.enchantments.throwback.listeners.ThrowbackListener
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import org.bukkit.inventory.EquipmentSlotGroup

@Suppress("UnstableApiUsage")
@AutoService(ThrowBackEnchantment::class)
class ThrowbackEnchantmentImpl : AbstractCustomEnchantment(
    key = key("surf", "throwback"),
    displayName = text("Throwback"),
    rarity = Rarity.EPIC,
    description = { level ->
        line {
            val bonusDamage = level * DAMAGE_BOOST_PER_LEVEL
            darkSpacer("Erhöht den Schaden um ")
            variableValue(bonusDamage.toString().removeSuffix(".0"))
            darkSpacer(" Punkte")
        }

        line {
            darkSpacer("Zieht das getroffene Monster mit Stärke ")
            variableValue("${level * THROWBACK_PER_LEVEL}")
        }
    },
    supportedItems = CustomItemTypeTags.THROWBACK_KEY.tagKey,
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
    exclusiveWith = setOf(),
    maxLevel = 5,
    listeners = objectSetOf(ThrowbackListener)
), ThrowBackEnchantment {
    companion object {
        const val DAMAGE_BOOST_PER_LEVEL = 1.5
        const val THROWBACK_PER_LEVEL = 5
    }
}