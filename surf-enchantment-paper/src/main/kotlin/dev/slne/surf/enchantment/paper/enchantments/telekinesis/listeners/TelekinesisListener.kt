package dev.slne.surf.enchantment.paper.enchantments.telekinesis.listeners

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.enchantment.api.enchantments.telekinesis.PostTelekinesisItemEvent
import dev.slne.surf.enchantment.api.enchantments.telekinesis.PreTelekinesisItemEvent
import dev.slne.surf.enchantment.api.enchantments.telekinesis.TelekinesisEnchantment
import dev.slne.surf.enchantment.api.utils.hasCustomEnchantment
import dev.slne.surf.enchantment.paper.plugin
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
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerShearEntityEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds


object TelekinesisListener : Listener {

    private data class SuppressedVehicle(val playerUuid: UUID, val vehicleLocation: Location)

    private val telekinesisTargets: MutableMap<UUID, SuppressedVehicle> =
        ConcurrentHashMap<UUID, SuppressedVehicle>()

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.isCancelled) return

        val player = event.player

        if (!player.inventory.itemInMainHand.hasCustomEnchantment<TelekinesisEnchantment>()) return

        player.giveExp(event.expToDrop, true)
        event.expToDrop = 0
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockDrop(event: BlockDropItemEvent) {
        if (event.isCancelled) return

        val player = event.player
        if (!player.inventory.itemInMainHand.hasCustomEnchantment<TelekinesisEnchantment>()) return

        val dropLocation = event.block.location.clone().add(0.5, 0.5, 0.5)
        val drops = event.items.map { it.itemStack }

        if (!addDropsToInventory(player, drops, event, dropLocation)) {
            event.items.clear()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityDeath(event: EntityDeathEvent) {
        if (event.isCancelled) return

        val player = event.entity.killer ?: return
        if (!player.inventory.itemInMainHand.hasCustomEnchantment<TelekinesisEnchantment>()) return

        val dropLocation = event.entity.location.clone()
        val drops = event.drops.toList()

        if (!addDropsToInventory(player, drops, event, dropLocation)) {
            event.drops.clear()
        }

        player.giveExp(event.droppedExp, true)
        event.droppedExp = 0
    }

    @EventHandler
    fun onVehicleDestroy(event: VehicleDestroyEvent) {
        val player = event.attacker as? Player ?: return
        if (!player.inventory.itemInMainHand.hasCustomEnchantment<TelekinesisEnchantment>()) return

        val vehicle = event.vehicle
        val dropLocation = vehicle.location.clone()

        telekinesisTargets[vehicle.uniqueId] = SuppressedVehicle(player.uniqueId, dropLocation)

        plugin.launch(plugin.entityDispatcher(vehicle)) {
            delay(1.ticks.milliseconds)
            telekinesisTargets.remove(vehicle.uniqueId)
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onItemSpawn(event: ItemSpawnEvent) {
        val loc = event.location

        for (entry in telekinesisTargets.entries) {
            val (playerUuid, vehicleLocation) = entry.value
            val player = server.getPlayer(playerUuid) ?: continue

            if (loc.world == player.world && loc.world == vehicleLocation.world && loc.distanceSquared(
                    vehicleLocation
                ) < 4
            ) {
                val stack = event.entity.itemStack
                val drops = listOf(stack)

                // Remove the entry and cancel the event BEFORE calling addDropsToInventory
                // to prevent infinite recursion: if the inventory is full, dropItemNaturally
                // would fire another ItemSpawnEvent at the same location, re-triggering this handler.
                telekinesisTargets.remove(entry.key)

                if (addDropsToInventory(
                        player = player,
                        drops = drops,
                        originEvent = event,
                        dropLocation = vehicleLocation.clone()
                    )
                ) {
                    event.isCancelled = true
                }

                break
            }
        }
    }

    @EventHandler
    fun onPlayerShearEntity(event: PlayerShearEntityEvent) {
        val player = event.player
        if (!player.inventory.itemInMainHand.hasCustomEnchantment<TelekinesisEnchantment>()) return

        val dropLocation = event.entity.location.clone()
        val drops = event.drops.toList()

        if (addDropsToInventory(player, drops, event, dropLocation)) {
            event.drops = emptyList()
        }
    }

    private fun addDropsToInventory(
        player: Player,
        drops: List<ItemStack>,
        originEvent: Event,
        dropLocation: Location
    ): Boolean {
        val preEvent = PreTelekinesisItemEvent(
            player = player,
            itemStacks = drops.toMutableList()
        )

        if (!preEvent.callEvent()) {
            return false
        }

        val modifiedDrops = preEvent.itemStacks

        modifiedDrops.forEach { drop ->
            val notAdded = player.inventory.addItem(drop)

            val postTelekinesisItemEvent = PostTelekinesisItemEvent(
                player = player,
                itemStack = drop,
                notAddedToInventory = notAdded.toMap(),
                originEvent = originEvent
            )
            postTelekinesisItemEvent.callEvent()

            notAdded.values.forEach { item ->
                dropLocation.world.dropItem(dropLocation, item).velocity = Vector(0, 0, 0)
            }
        }

        return true
    }
}
