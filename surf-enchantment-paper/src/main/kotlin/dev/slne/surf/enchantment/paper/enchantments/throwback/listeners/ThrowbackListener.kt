package dev.slne.surf.enchantment.paper.enchantments.throwback.listeners

import dev.slne.surf.enchantment.api.enchantments.ThrowBackEnchantment
import dev.slne.surf.enchantment.paper.enchantments.throwback.ThrowbackEnchantmentImpl
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Trident
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

object ThrowbackListener : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onDamage(event: EntityDamageByEntityEvent) {
        val entity = event.entity as? LivingEntity ?: return

        if (entity is Player) {
            return
        }

        val (player, item) = when (val damager = event.damager) {
            is Player -> damager to damager.inventory.itemInMainHand
            is Trident -> {
                val shooter = damager.shooter as? Player ?: return
                shooter to damager.itemStack
            }

            else -> return
        }

        val level = item.getEnchantmentLevel(ThrowBackEnchantment.bukkitEnchantment)
        if (level <= 0) {
            return
        }

        val bonusDamage = level * ThrowbackEnchantmentImpl.DAMAGE_BOOST_PER_LEVEL
        event.damage += bonusDamage

        val delta = player.location.toVector().subtract(entity.location.toVector())
        if (delta.lengthSquared() < 1.0E-4) {
            return
        }

        val strength = level * ThrowbackEnchantmentImpl.THROWBACK_PER_LEVEL / 10.0
        entity.velocity = delta.normalize().multiply(strength).setY(0.45 + level * 0.04)

        player.spawnParticle(Particle.TRIAL_SPAWNER_DETECTION_OMINOUS, entity.location, 50)
    }
}