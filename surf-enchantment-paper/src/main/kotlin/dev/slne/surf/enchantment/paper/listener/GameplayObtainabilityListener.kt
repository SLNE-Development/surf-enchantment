@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.listener

import dev.slne.surf.enchantment.api.enchantment.EnchantmentManager
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemEnchantments
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.world.LootGenerateEvent
import org.bukkit.inventory.ItemStack

object GameplayObtainabilityListener : Listener {
    private val blockedEnchantments: Set<Enchantment> by lazy {
        EnchantmentManager.customEnchantments
            .filterNot { it.obtainableFromGameplay }
            .mapTo(mutableSetOf()) { it.bukkitEnchantment }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onLootGenerate(event: LootGenerateEvent) {
        if (blockedEnchantments.isEmpty()) return

        event.loot.forEach { item ->
            item.removeBlockedEnchantments()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEnchantItem(event: EnchantItemEvent) {
        if (blockedEnchantments.isEmpty()) return

        event.enchantsToAdd.keys.removeAll(blockedEnchantments)
    }

    private fun ItemStack.removeBlockedEnchantments() {
        val enchants = getData(DataComponentTypes.ENCHANTMENTS)
        if (enchants != null) {
            val original = enchants.enchantments()
            val filtered = original.filterKeys { it !in blockedEnchantments }
            if (filtered.size < original.size) {
                setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(filtered))
            }
        }

        val storedEnchants = getData(DataComponentTypes.STORED_ENCHANTMENTS)
        if (storedEnchants != null) {
            val original = storedEnchants.enchantments()
            val filtered = original.filterKeys { it !in blockedEnchantments }
            if (filtered.size < original.size) {
                setData(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantments.itemEnchantments(filtered))
            }
        }
    }
}
