@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.enchantments.veinminer

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.rarity.Rarity
import dev.slne.surf.enchantment.api.enchantment.AbstractCustomEnchantment
import dev.slne.surf.enchantment.api.enchantments.VeinMinerEnchantment
import dev.slne.surf.enchantment.api.utils.CustomItemTypeTags
import dev.slne.surf.enchantment.paper.enchantments.veinminer.listeners.VeinMinerListener
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import net.kyori.adventure.text.Component.text
import org.bukkit.enchantments.Enchantment

@AutoService(VeinMinerEnchantment::class)
class VeinMinerEnchantmentImpl : AbstractCustomEnchantment(
    key = key("surf", "vein_miner"),
    displayName = text("Vein Miner"),
    rarity = Rarity.EPIC,
    description = {
        line { darkSpacer("Baut zusammenhändende Erzadern ab.") }
        line {
            darkSpacer("Die Abklingzeit beträgt")
            appendSpace()
            variableValue("2 Sekunden")
            appendSpace()
            darkSpacer("pro abgebautem Erz.")
        }
    },
    supportedItems = CustomItemTypeTags.VEIN_MINER_KEY.tagKey,
    weight = 2,
    minimumCost = EnchantmentRegistryEntry.EnchantmentCost.of(
        15,
        9
    ),
    maximumCost = EnchantmentRegistryEntry.EnchantmentCost.of(
        65,
        9
    ),
    exclusiveWith = setOf(Enchantment.FORTUNE.key()),
    tags = setOf(
        EnchantmentTagKeys.ON_RANDOM_LOOT,
        EnchantmentTagKeys.TREASURE
    ),
    listeners = setOf(VeinMinerListener),
    jobs = setOf(VeinMinerListener.cooldownHandler)
), VeinMinerEnchantment {
    companion object {
        const val MAX_ORES_TO_MINE = 10
        const val COOLDOWN_PER_ORE_MS = 2000
    }
}