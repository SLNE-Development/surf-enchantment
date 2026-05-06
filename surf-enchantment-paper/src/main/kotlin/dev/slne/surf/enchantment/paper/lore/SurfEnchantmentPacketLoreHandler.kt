@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.lore

import dev.slne.surf.api.core.messages.adventure.plain
import dev.slne.surf.api.core.rarity.Rarity
import dev.slne.surf.api.core.util.mutableObject2IntMapOf
import dev.slne.surf.api.paper.packet.lore.SurfPaperPacketLoreHandler
import dev.slne.surf.api.paper.packet.lore.SurfPaperPacketLorePriority
import dev.slne.surf.enchantment.api.enchantment.EnchantmentManager
import dev.slne.surf.enchantment.api.utils.Enchantable
import dev.slne.surf.enchantment.paper.utils.VanillaEnchantmentMap
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.persistence.PersistentDataContainerView
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

internal object SurfEnchantmentPacketLoreHandler : SurfPaperPacketLoreHandler {
    private val comparator = compareByDescending<Enchantable> { it.rarity }
        .thenByDescending { it.displayName.plain() }

    override val priority = SurfPaperPacketLorePriority.FIRST

    override fun handleLore(
        loreToDisplay: MutableList<Component>,
        pdc: PersistentDataContainerView,
        itemStack: ItemStack
    ) {
        val enchantments = mutableObject2IntMapOf<Enchantment>().apply {
            itemStack.getData(DataComponentTypes.ENCHANTMENTS)
                ?.enchantments()
                ?.let(::putAll)

            itemStack.getData(DataComponentTypes.STORED_ENCHANTMENTS)
                ?.enchantments()
                ?.let(::putAll)
        }

        if (enchantments.isEmpty()) return

        itemStack.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_STORED_ENCHANTS)

        val shouldDisplayEnchantmentDescription = enchantments.size < 5
        val sortedEnchantments = Object2IntRBTreeMap(comparator)

        enchantments.object2IntEntrySet().fastForEach(fun(entry) {
            val enchantment = entry.key
            val level = entry.intValue

            val customEnchantment = VanillaEnchantmentMap.getByKey(enchantment.key)
                ?: EnchantmentManager.findByBukkitEnchantment(entry.key)
                ?: return

            sortedEnchantments.put(customEnchantment.withEffectiveRarity(level), level)
        })

        val iterator = sortedEnchantments.object2IntEntrySet().iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            val enchantment = next.key
            val level = next.intValue
            loreToDisplay.addAll(enchantment.buildLore(level, shouldDisplayEnchantmentDescription))
        }
    }

    private fun Enchantable.withEffectiveRarity(level: Int): Enchantable {
        val max = maxLevel ?: return this
        if (level <= max) return this

        return RarityOverrideEnchantable(this, Rarity.MYTHIC)
    }

    private class RarityOverrideEnchantable(
        private val delegate: Enchantable,
        override val rarity: Rarity,
    ) : Enchantable {
        override val key: Key get() = delegate.key
        override val displayName: Component get() = delegate.displayName
        override val description get() = delegate.description
        override val maxLevel get() = delegate.maxLevel
    }
}
