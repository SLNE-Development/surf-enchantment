package dev.slne.surf.enchantment.paper.enchantments.unbreakablewish.listeners

import dev.slne.surf.enchantment.paper.enchantments.unbreakablewish.BrokenItemState
import dev.slne.surf.enchantment.paper.plugin
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.EquipmentSlot

object BrokenItemStateListener : Listener {
    private val SYNCED_SLOTS = listOf(
        EquipmentSlot.HAND,
        EquipmentSlot.OFF_HAND,
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET,
    )

    /**
     * Vanilla has already set the damage to the maximum and shrinks the stack by one
     * right after this event. The item is a mirror, so raising its amount leaves exactly
     * one item behind, while vanilla still plays the break sound and animation.
     *
     * Runs at MONITOR so no other listener sees the temporary amount of two.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onItemBreak(event: PlayerItemBreakEvent) {
        val item = event.brokenItem
        if (!BrokenItemState.hasWish(item)) return

        item.amount += 1
        BrokenItemState.sync(item)
    }

    @EventHandler(ignoreCancelled = true)
    fun onItemDamage(event: PlayerItemDamageEvent) {
        if (BrokenItemState.isBroken(event.item)) {
            event.isCancelled = true
        }
    }

    /**
     * Catches repairs that happen while the item is equipped (Mending, other plugins, ...).
     *
     * The equipment snapshot vanilla compares against is taken after this event, so a
     * change made here would never be detected and the attributes never re-applied.
     * The sync therefore runs one tick later on the player's scheduler.
     */
    @EventHandler
    fun onEquipmentChanged(event: EntityEquipmentChangedEvent) {
        val player = event.entity as? Player ?: return
        val needsSync = event.equipmentChanges.values.any { BrokenItemState.needsSync(it.newItem()) }
        if (!needsSync) return

        player.scheduler.run(plugin, {
            SYNCED_SLOTS.forEach { slot -> BrokenItemState.sync(player.inventory.getItem(slot)) }
        }, null)
    }
}
