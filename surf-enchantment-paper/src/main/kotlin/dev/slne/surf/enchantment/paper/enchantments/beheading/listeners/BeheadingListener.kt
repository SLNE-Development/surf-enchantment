@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.enchantments.beheading.listeners

import dev.slne.surf.api.core.util.random
import dev.slne.surf.enchantment.api.enchantments.BeheadingEnchantment
import dev.slne.surf.enchantment.api.utils.getThisActiveEnchantmentOrNull
import dev.slne.surf.enchantment.paper.enchantments.beheading.BeheadingEnchantmentImpl
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemType

object BeheadingListener : Listener {
    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        val killer = event.entity.killer ?: return
        val (level) = killer.getThisActiveEnchantmentOrNull<BeheadingEnchantment>() ?: return

        val chance = level * BeheadingEnchantmentImpl.CHANCE_PER_LEVEL

        if (random.nextInt(0, 100) < chance) {
            event.drops.add(entity.type.getHead().createItemStack())
        }
    }

    private fun EntityType.getHead() = when (this) {
        EntityType.ZOMBIE -> ItemType.ZOMBIE_HEAD
        EntityType.CREEPER -> ItemType.CREEPER_HEAD
        EntityType.SKELETON -> ItemType.SKELETON_SKULL
        EntityType.PIGLIN -> ItemType.PIGLIN_HEAD
        EntityType.ENDER_DRAGON -> ItemType.DRAGON_HEAD
        EntityType.WITHER_SKELETON -> ItemType.WITHER_SKELETON_SKULL
        else -> ItemType.AIR
    }
}
