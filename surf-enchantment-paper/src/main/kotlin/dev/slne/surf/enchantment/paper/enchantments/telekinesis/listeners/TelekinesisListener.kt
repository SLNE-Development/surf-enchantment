package dev.slne.surf.enchantment.paper.enchantments.telekinesis.listeners

import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.enchantment.api.enchantments.telekinesis.PostTelekinesisItemEvent
import dev.slne.surf.enchantment.api.enchantments.telekinesis.PreTelekinesisItemEvent
import dev.slne.surf.enchantment.api.enchantments.telekinesis.TelekinesisEnchantment
import dev.slne.surf.enchantment.api.utils.hasCustomEnchantment
import dev.slne.surf.enchantment.paper.plugin
import dev.slne.surf.enchantment.paper.utils.addItemPreferringOffhand
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.player.PlayerShearEntityEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*
import java.util.concurrent.ConcurrentHashMap


object TelekinesisListener : Listener {

    private data class SuppressedVehicle(val playerUuid: UUID, val vehicleLocation: Location)

    private val telekinesisTargets: MutableMap<UUID, SuppressedVehicle> =
        ConcurrentHashMap<UUID, SuppressedVehicle>()

    private val captureDepth: ThreadLocal<Int> = ThreadLocal.withInitial { 0 }

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

        if (addDropsToInventory(player, drops, event, dropLocation)) {
            event.items.clear()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityDeath(event: EntityDeathEvent) {
        if (event.isCancelled) return

        if (event.entityType == EntityType.PLAYER) {
            return
        }

        val player = event.entity.killer ?: return
        if (!player.inventory.itemInMainHand.hasCustomEnchantment<TelekinesisEnchantment>()) return

        val dropLocation = event.entity.location.clone()
        val drops = event.drops.toList()

        if (addDropsToInventory(player, drops, event, dropLocation)) {
            event.drops.clear()
        }

        player.giveExp(event.droppedExp, true)
        event.droppedExp = 0
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onHangingBreakByEntity(event: HangingBreakByEntityEvent) {
        val player = event.remover as? Player ?: return
        if (!player.inventory.itemInMainHand.hasCustomEnchantment<TelekinesisEnchantment>()) return

        val hanging = event.entity
        val dropLocation = hanging.location.clone()
        val drops = mutableListOf<ItemStack>()

        when (hanging) {
            is ItemFrame -> {
                val material =
                    if (hanging is GlowItemFrame) Material.GLOW_ITEM_FRAME else Material.ITEM_FRAME
                drops.add(ItemStack(material))

                val innerItem = hanging.item
                if (innerItem.type != Material.AIR) {
                    drops.add(innerItem.clone())
                }
            }

            is Painting -> {
                drops.add(ItemStack(Material.PAINTING))
            }

            else -> return
        }

        if (addDropsToInventory(player, drops, event, dropLocation)) {
            hanging.remove()
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onVehicleDestroy(event: VehicleDestroyEvent) {
        val player = event.attacker as? Player ?: return
        if (!player.inventory.itemInMainHand.hasCustomEnchantment<TelekinesisEnchantment>()) return

        val vehicle = event.vehicle
        val vehicleId = vehicle.uniqueId
        val dropLocation = vehicle.location.clone()

        telekinesisTargets[vehicleId] = SuppressedVehicle(player.uniqueId, dropLocation)

        // The vehicle drops its contents and itself synchronously right after this event. Removing the
        // entry on the next tick of the owning region keeps it for all of those drops and can never run
        // in the middle of the destruction.
        server.regionScheduler.runDelayed(plugin, dropLocation, { telekinesisTargets.remove(vehicleId) }, 1L)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onItemSpawn(event: ItemSpawnEvent) {
        // Spawns caused while a captured item is being processed (leftover drops, items spawned by
        // Pre/Post listeners) must not be captured again, otherwise a full inventory recurses forever.
        if (captureDepth.get() > 0) return

        val loc = event.location

        for (entry in telekinesisTargets.entries) {
            val (playerUuid, vehicleLocation) = entry.value
            if (loc.world != vehicleLocation.world || loc.distanceSquared(vehicleLocation) >= 4) continue

            val player = server.getPlayer(playerUuid) ?: continue
            if (!canReceive(player, vehicleLocation)) continue

            captureDepth.set(captureDepth.get() + 1)
            try {
                val drops = listOf(event.entity.itemStack.clone())

                addDropsToInventory(player, drops, event, vehicleLocation.clone()) {
                    if (!canReceive(player, vehicleLocation)) return@addDropsToInventory false

                    // Removing the entity as well as cancelling prevents a later listener from
                    // un-cancelling the spawn and duplicating the item that is about to be inserted.
                    event.isCancelled = true
                    event.entity.remove()
                    true
                }
            } finally {
                captureDepth.set(captureDepth.get() - 1)
            }

            break
        }
    }

    /**
     * Checks whether [player] may receive captured vehicle drops. Region ownership is checked first
     * because the other checks read entity state, which Folia only allows from the owning thread.
     */
    private fun canReceive(player: Player, vehicleLocation: Location): Boolean =
        server.isOwnedByCurrentRegion(player) &&
                !player.isDead &&
                player.isValid &&
                player.world == vehicleLocation.world

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
        dropLocation: Location,
        beforeInsert: () -> Boolean = { true }
    ): Boolean {
        val preEvent = PreTelekinesisItemEvent(
            player = player,
            itemStacks = drops.toMutableList()
        )

        if (!preEvent.callEvent()) {
            return false
        }

        if (!beforeInsert()) {
            return false
        }

        val modifiedDrops = preEvent.itemStacks

        modifiedDrops.forEach { drop ->
            val notAdded = player.inventory.addItemPreferringOffhand(drop)

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
