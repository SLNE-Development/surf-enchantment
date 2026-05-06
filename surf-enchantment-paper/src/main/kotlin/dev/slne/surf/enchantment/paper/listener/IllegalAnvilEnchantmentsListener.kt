@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.listener

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack

object IllegalAnvilEnchantmentsListener : Listener {
    @EventHandler
    fun onAnvilPrepare(event: PrepareAnvilEvent) {
        val left = event.inventory.firstItem ?: return
        val right = event.inventory.secondItem ?: return

        if (!isBook(left) || !isBook(right)) {
            return
        }

        val leftEnchants = getStoredEnchants(left)
        val rightEnchants = getStoredEnchants(right)

        for ((enchant, levelLeft) in leftEnchants) {
            val levelRight = rightEnchants[enchant] ?: continue

            val resultLevel =
                if (levelLeft == levelRight) levelLeft + 1 else maxOf(levelLeft, levelRight)

            if (resultLevel > enchant.maxLevel) {
                event.result = null
                return
            }
        }
    }

    private fun isBook(item: ItemStack) = item.hasData(DataComponentTypes.STORED_ENCHANTMENTS)

    private fun getStoredEnchants(item: ItemStack): Map<Enchantment, Int> {
        return item.getData(DataComponentTypes.STORED_ENCHANTMENTS)?.enchantments() ?: emptyMap()
    }
}