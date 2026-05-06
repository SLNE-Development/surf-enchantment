@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.enchantments.holedigger

import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.rarity.Rarity
import dev.slne.surf.enchantment.api.enchantment.AbstractCustomEnchantment
import dev.slne.surf.enchantment.api.enchantments.HoleDiggerEnchantment
import dev.slne.surf.enchantment.api.enchantments.VeinMinerEnchantment
import dev.slne.surf.enchantment.api.utils.CustomItemTypeTags
import dev.slne.surf.enchantment.paper.enchantments.holedigger.listeners.HoleDiggerListener
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import net.kyori.adventure.text.Component.text
import org.bukkit.enchantments.Enchantment

//@AutoService(HoleDiggerEnchantment::class)
class HoleDiggerEnchantmentImpl : AbstractCustomEnchantment(
    key = key("surf", "hole_digger"),
    displayName = text("Hole Digger"),
    rarity = Rarity.MYTHIC,
    description = {
        line { darkSpacer("Gräbt ein Loch in definierter Größe") }
    },
    supportedItems = CustomItemTypeTags.HOLE_DIGGER_KEY.tagKey,
    weight = 2,
    minimumCost = EnchantmentRegistryEntry.EnchantmentCost.of(
        15,
        9
    ),
    maximumCost = EnchantmentRegistryEntry.EnchantmentCost.of(
        65,
        9
    ),
    exclusiveWith = setOf(
        Enchantment.FORTUNE.key(),
        VeinMinerEnchantment.key
    ),
    tags = setOf(EnchantmentTagKeys.TRADEABLE),
    listeners = setOf(HoleDiggerListener),
    jobs = setOf(HoleDiggerListener.cooldownHandler)
), HoleDiggerEnchantment
