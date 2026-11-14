@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.enchantments.unbreakablewish.listeners

import dev.slne.surf.enchantment.paper.enchantments.unbreakablewish.BrokenItemState
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.inventory.PrepareGrindstoneEvent
import org.bukkit.inventory.ItemStack

object BrokenItemRepairListener : Listener {
    /**
     * A broken item may only be renamed or repaired with something that carries no
     * enchantments (repair material or an unenchanted item of the same type).
     * A repaired result gets its components and enchantments back.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAnvilPrepare(event: PrepareAnvilEvent) {
        val first = event.inventory.firstItem
        val second = event.inventory.secondItem
        if (!BrokenItemState.isBroken(first) && !BrokenItemState.isBroken(second)) return

        if (BrokenItemState.isBroken(second) || second.hasAnyEnchantments()) {
            event.result = null
            return
        }

        val result = event.result?.takeUnless { it.isEmpty } ?: return
        BrokenItemState.sync(result)
        event.result = result
    }

    /**
     * The grindstone only removes active enchantments. The dormant ones are dropped too,
     * otherwise they would come back once the item is restored.
     */
    @EventHandler
    fun onGrindstonePrepare(event: PrepareGrindstoneEvent) {
        val result = event.result?.takeUnless { it.isEmpty } ?: return
        if (!BrokenItemState.isBroken(result)) return

        BrokenItemState.discardDormantEnchantments(result)
        BrokenItemState.sync(result)
        event.result = result
    }

    private fun ItemStack?.hasAnyEnchantments(): Boolean {
        if (this == null || isEmpty) return false

        val enchantments = getData(DataComponentTypes.ENCHANTMENTS)?.enchantments().orEmpty()
        val storedEnchantments = getData(DataComponentTypes.STORED_ENCHANTMENTS)?.enchantments().orEmpty()
        return enchantments.isNotEmpty() || storedEnchantments.isNotEmpty()
    }
}
