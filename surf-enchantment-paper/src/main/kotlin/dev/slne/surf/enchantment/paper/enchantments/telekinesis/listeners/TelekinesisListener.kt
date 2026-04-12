package dev.slne.surf.enchantment.paper.enchantments.telekinesis.listeners

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import dev.slne.surf.enchantment.api.enchantments.telekinesis.PostTelekinesisItemEvent
import dev.slne.surf.enchantment.api.enchantments.telekinesis.TelekinesisEnchantment
import dev.slne.surf.enchantment.api.utils.hasCustomEnchantment
import dev.slne.surf.enchantment.paper.plugin
import dev.slne.surf.enchantment.paper.utils.VehicleDrops
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.player.PlayerShearEntityEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object TelekinesisListener : Listener {

    private val suppressedVehicles: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    @EventHandler(priority = EventPriority.LOW)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player

        if (!player.inventory.itemInMainHand.hasCustomEnchantment<TelekinesisEnchantment>()) return

        player.giveExp(event.expToDrop, true)
        event.expToDrop = 0
    }

    @EventHandler
    fun onBlockDrop(event: BlockDropItemEvent) {
        val player = event.player
        if (!player.inventory.itemInMainHand.hasCustomEnchantment<TelekinesisEnchantment>()) return

        val dropLocation = event.block.location.clone().add(0.5, 0.5, 0.5)
        val drops = event.items.map { it.itemStack }

        addDropsToInventory(player, drops, event, dropLocation)

        event.items.clear()
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val player = event.entity.killer ?: return
        if (!player.inventory.itemInMainHand.hasCustomEnchantment<TelekinesisEnchantment>()) return

        val dropLocation = event.entity.location.clone()
        val drops = event.drops.toList()

        addDropsToInventory(player, drops, event, dropLocation)

        player.giveExp(event.droppedExp, true)
        event.droppedExp = 0
        event.drops.clear()
    }

    @EventHandler
    fun onVehicleDestroy(event: VehicleDestroyEvent) {
        val player = event.attacker as? Player ?: return
        if (!player.inventory.itemInMainHand.hasCustomEnchantment<TelekinesisEnchantment>()) return

        val vehicle = event.vehicle
        val dropLocation = vehicle.location.clone()
        val drops = VehicleDrops.getDrops(vehicle)

        if (vehicle is InventoryHolder) {
            vehicle.inventory.clear()
        }

        val vehicleId = vehicle.uniqueId
        suppressedVehicles.add(vehicleId)

        addDropsToInventory(player, drops, event, dropLocation)

        plugin.launch(plugin.entityDispatcher(vehicle)) {
            delay(1.ticks)
            suppressedVehicles.remove(vehicleId)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onEntityDropItem(event: EntityDropItemEvent) {
        if (event.entity.uniqueId in suppressedVehicles) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerShearEntity(event: PlayerShearEntityEvent) {
        val player = event.player
        if (!player.inventory.itemInMainHand.hasCustomEnchantment<TelekinesisEnchantment>()) return

        val dropLocation = event.entity.location.clone()
        val drops = event.drops.toList()

        addDropsToInventory(player, drops, event, dropLocation)

        event.drops.clear()
    }

    private fun addDropsToInventory(
        player: Player,
        drops: List<ItemStack>,
        originEvent: Event,
        dropLocation: Location
    ) {
        drops.forEach { drop ->
            val notAdded = player.inventory.addItem(drop)

            val postTelekinesisItemEvent = PostTelekinesisItemEvent(
                player = player,
                itemStack = drop,
                notAddedToInventory = notAdded.toMap(),
                originEvent = originEvent
            )

            notAdded.clear()
            notAdded.putAll(postTelekinesisItemEvent.notAddedToInventory)

            notAdded.values.forEach { item ->
                dropLocation.world.dropItemNaturally(dropLocation, item)
            }
        }
    }
}