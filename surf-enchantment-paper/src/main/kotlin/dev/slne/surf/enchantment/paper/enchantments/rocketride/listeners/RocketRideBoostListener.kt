@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.enchantments.rocketride.listeners

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.enchantment.api.enchantment.EnchantmentManager
import dev.slne.surf.enchantment.api.enchantments.RocketRideEnchantment
import dev.slne.surf.enchantment.api.utils.hasCustomEnchantment
import dev.slne.surf.enchantment.paper.enchantments.rocketride.RocketRideEnchantmentImpl
import dev.slne.surf.enchantment.paper.enchantments.rocketride.RocketRideBoostService
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
import org.bukkit.persistence.PersistentDataType
import org.bukkit.Sound as BukkitSound

object RocketRideBoostListener : Listener {
    private val specialHappyGhastKey = NamespacedKey("surf", "rocket-ride-happy-ghast")
    val cooldownHandler = CooldownHandler(notReadyMessage = { secondsLeft ->
        error("Der Ghast ist noch außer puste! Er ist in")
        appendSpace()
        variableValue("$secondsLeft Sekunden")
        appendSpace()
        error("wieder fit.")
    })

    init {
        cooldownHandler.registerExpirationListener { uuid ->
            withContext(EnchantmentManager.globalRegionDispatcher) {
                val entity = server.getEntity(uuid) ?: return@withContext

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

    @EventHandler
    fun onAddHarness(event: PlayerInteractEntityEvent) {
        val player = event.player
        val happyGhast = event.rightClicked as? HappyGhast ?: return

        val itemInMain = player.inventory.itemInMainHand
        if (!itemInMain.hasCustomEnchantment<RocketRideEnchantment>()) return

        happyGhast.persistentDataContainer.set(specialHappyGhastKey, PersistentDataType.BYTE, 1)
    }

    @EventHandler
    fun onRemoveHarness(event: EntityDropItemEvent) {
        val happyGhast = event.entity as? HappyGhast ?: return

        if (!happyGhast.persistentDataContainer.has(
                specialHappyGhastKey,
                PersistentDataType.BYTE
            )
        ) return
        happyGhast.persistentDataContainer.remove(specialHappyGhastKey)

        event.itemDrop.itemStack.addEnchantment(RocketRideEnchantment.bukkitEnchantment, 1)
    }

    @EventHandler
    fun onUseRocketToBoost(event: PlayerInteractEvent) {
        val player = event.player
        val happyGhast = player.vehicle as? HappyGhast ?: return
        if (!happyGhast.persistentDataContainer.has(
                specialHappyGhastKey,
                PersistentDataType.BYTE
            )
        ) return

        val item = player.inventory.itemInMainHand
        if (item.type != Material.FIREWORK_ROCKET) return

        if (happyGhast.passengers.firstOrNull() != player) {
            player.sendActionBar(buildText { error("Du hältst nicht die Zügel des Ghasts!") })
            return
        }

        if (!cooldownHandler.checkCooldown(happyGhast.uniqueId, player)) return

        val tier = (item.getData(DataComponentTypes.FIREWORKS)?.flightDuration() ?: return).coerceIn(1, 3)
        val boost = RocketRideEnchantmentImpl.boostForLevel(tier)

        RocketRideBoostService.startBoost(
            ghast = happyGhast,
            rider = player,
            power = RocketRideEnchantmentImpl.BASE_POWER * boost.multiplier,
            upward = boost.upward,
            durationTicks = RocketRideEnchantmentImpl.durationTicksForLevel(tier)
        )

        if (player.gameMode != GameMode.CREATIVE) {
            item.amount -= 1
        }

        //cooldownHandler.applyCooldown(player, happyGhast.uniqueId, boost.cooldownSeconds.seconds) TODO: Implement

        happyGhast.passengers.forEach { passenger ->
            passenger.sendActionBar(buildText { success("Der Happy Ghast wurde geboostet!") })
            passenger.playSound {
                type(BukkitSound.ENTITY_FIREWORK_ROCKET_BLAST)
                source(Sound.Source.NEUTRAL)
                pitch(1.0f)
            }
        }
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
