@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.enchantments.lumberjack

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.rarity.Rarity
import dev.slne.surf.enchantment.api.enchantment.AbstractCustomEnchantment
import dev.slne.surf.enchantment.api.enchantments.LumberJackEnchantment
import dev.slne.surf.enchantment.api.utils.CustomItemTypeTags
import dev.slne.surf.enchantment.paper.enchantments.lumberjack.listeners.LumberJackListener
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import net.kyori.adventure.text.Component.text

@AutoService(LumberJackEnchantment::class)
class LumberjackEnchantmentImpl : AbstractCustomEnchantment(
    key = key("surf", "lumberjack"),
    displayName = text("Lumberjack"),
    rarity = Rarity.EPIC,
    description = { level ->
        val blocksToMine = blocksForLevel(level)
        val cooldownSeconds = COOLDOWN_MS / 1000

        line {
            darkSpacer("Ermöglicht es dir alle")
            appendSpace()
            variableValue("$cooldownSeconds Sekunden")
        }
        line {
            darkSpacer("bis zu")
            appendSpace()
            variableValue("$blocksToMine Blöcke")
            appendSpace()
            darkSpacer("eines Baumes auf einmal abzubauen.")
        }
    },
    supportedItems = CustomItemTypeTags.LUMBERJACK_KEY.tagKey,
    weight = 2,
    minimumCost = EnchantmentRegistryEntry.EnchantmentCost.of(
        15,
        9
    ),
    maximumCost = EnchantmentRegistryEntry.EnchantmentCost.of(
        65,
        9
    ),
    tags = setOf(
        EnchantmentTagKeys.IN_ENCHANTING_TABLE,
    ),
    maxLevel = MAX_LEVEL,
    listeners = setOf(LumberJackListener),
    jobs = setOf(LumberJackListener.cooldownHandler)
), LumberJackEnchantment {
    companion object {
        const val MAX_LEVEL = 5
        const val BLOCKS_PER_LEVEL = 10
        const val COOLDOWN_MS = 20_000
        const val INCLUDE_LEAVES = false

        fun blocksForLevel(level: Int) = level.coerceAtLeast(1) * BLOCKS_PER_LEVEL
    }
}