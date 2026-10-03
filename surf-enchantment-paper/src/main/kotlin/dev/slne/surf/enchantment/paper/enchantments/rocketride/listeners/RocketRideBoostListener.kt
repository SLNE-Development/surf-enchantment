@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.enchantments.rocketride.listeners

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.enchantment.api.enchantment.EnchantmentManager
import dev.slne.surf.enchantment.api.enchantments.RocketRideEnchantment
import dev.slne.surf.enchantment.api.utils.getThisEnchantmentOrNull
import dev.slne.surf.enchantment.paper.enchantments.rocketride.RocketRideBoostService
import dev.slne.surf.enchantment.paper.enchantments.rocketride.RocketRideEnchantmentImpl
import dev.slne.surf.enchantment.paper.utils.CooldownHandler
import io.papermc.paper.datacomponent.DataComponentTypes
import kotlinx.coroutines.withContext
import net.kyori.adventure.sound.Sound
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.HappyGhast
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import org.bukkit.Sound as BukkitSound

object RocketRideBoostListener : Listener {
    private val specialHappyGhastKey = NamespacedKey("surf", "rocket-ride-happy-ghast")
    val cooldownHandler = CooldownHandler(notReadyMessage = { secondsLeft ->
        appendErrorPrefix()
        error("Der Ghast ist noch außer puste! Er ist in")
        appendSpace()
        variableValue("$secondsLeft Sekunden")
        appendSpace()
        error("wieder fit.")
    })

    init {
        cooldownHandler.registerExpirationListener { uuid ->
            val entity = server.getEntity(uuid) ?: return@registerExpirationListener
            withContext(EnchantmentManager.entityDispatcher.invoke(entity)) {
                entity.passengers.filterIsInstance<Player>().forEach { passenger ->
                    passenger.sendActionBar(
                        buildText {
                            success("Der Happy Ghast ist wieder fit!")
                        }
                    )
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onAddHarness(event: PlayerInteractEntityEvent) {
        val player = event.player
        val happyGhast = event.rightClicked as? HappyGhast ?: return

        // Only an unharnessed ghast can be equipped; otherwise the stored level
        // could be overwritten and a higher level harness dropped on removal.
        if (!happyGhast.equipment.getItem(EquipmentSlot.BODY).isEmpty) return

        val itemInMain = player.inventory.itemInMainHand
        val (level) = itemInMain.getThisEnchantmentOrNull<RocketRideEnchantment>() ?: return

        happyGhast.persistentDataContainer.set(
            specialHappyGhastKey,
            PersistentDataType.BYTE,
            level.coerceIn(1, RocketRideEnchantmentImpl.MAX_LEVEL).toByte()
        )
    }

    @EventHandler
    fun onRemoveHarness(event: EntityDropItemEvent) {
        val happyGhast = event.entity as? HappyGhast ?: return

        val level = happyGhast.rocketRideLevel() ?: return
        happyGhast.persistentDataContainer.remove(specialHappyGhastKey)

        event.itemDrop.itemStack.addEnchantment(RocketRideEnchantment.bukkitEnchantment, level)
    }

    @EventHandler
    fun onUseRocketToBoost(event: PlayerInteractEvent) {
        val player = event.player
        val happyGhast = player.vehicle as? HappyGhast ?: return
        val level = happyGhast.rocketRideLevel() ?: return

        val item = player.inventory.itemInMainHand
        if (item.type != Material.FIREWORK_ROCKET) return

        if (happyGhast.passengers.firstOrNull() != player) {
            player.sendActionBar(buildText { error("Du hältst nicht die Zügel des Ghasts!") })
            return
        }

        if (!cooldownHandler.checkCooldown(happyGhast.uniqueId, player)) return

        val tier =
            (item.getData(DataComponentTypes.FIREWORKS)?.flightDuration() ?: return).coerceIn(1, RocketRideEnchantmentImpl.MAX_ROCKET_TIER)
        val boost = RocketRideEnchantmentImpl.boostForRocketTier(tier)

        RocketRideBoostService.startBoost(
            ghast = happyGhast,
            rider = player,
            power = RocketRideEnchantmentImpl.BASE_POWER * boost.multiplier,
            upward = boost.upward,
            durationTicks = RocketRideEnchantmentImpl.durationTicksForRocketTier(tier)
        )

        if (player.gameMode != GameMode.CREATIVE) {
            item.amount -= 1
        }

        cooldownHandler.applyCooldown(player, RocketRideEnchantmentImpl.cooldownForLevel(level), happyGhast.uniqueId)
        happyGhast.passengers.forEach { passenger ->
            passenger.sendActionBar(buildText { success("Der Happy Ghast wurde geboostet!") })
            passenger.playSound {
                type(BukkitSound.ENTITY_FIREWORK_ROCKET_BLAST)
                source(Sound.Source.NEUTRAL)
                pitch(1.0f)
            }
        }
    }

    private fun HappyGhast.rocketRideLevel(): Int? {
        val stored = persistentDataContainer.get(specialHappyGhastKey, PersistentDataType.BYTE)
            ?: return null

        return stored.toInt().coerceIn(1, RocketRideEnchantmentImpl.MAX_LEVEL)
    }

    @EventHandler
    fun onGhastDeath(event: EntityDeathEvent) {
        val happyGhast = event.entity as? HappyGhast ?: return
        if (!happyGhast.persistentDataContainer.has(
                specialHappyGhastKey,
                PersistentDataType.BYTE
            )
        ) return

        cooldownHandler.invalidateCooldown(happyGhast.uniqueId)

        if (RocketRideBoostService.isBoosting(happyGhast)) {
            RocketRideBoostService.stopBoost(happyGhast)
        }
    }
}
