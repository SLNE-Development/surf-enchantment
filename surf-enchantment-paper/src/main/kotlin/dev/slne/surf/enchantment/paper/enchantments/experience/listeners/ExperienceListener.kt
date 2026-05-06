package dev.slne.surf.enchantment.paper.enchantments.experience.listeners

import dev.slne.surf.enchantment.api.enchantments.ExperienceEnchantment
import dev.slne.surf.enchantment.api.utils.getThisActiveEnchantmentOrNull
import dev.slne.surf.enchantment.paper.enchantments.experience.ExperienceEnchantmentImpl
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerFishEvent

object ExperienceListener : Listener {

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        val (level) = killer.getThisActiveEnchantmentOrNull<ExperienceEnchantment>() ?: return

        event.droppedExp = calculateDrops(event.droppedExp, level).toInt()
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBreakBlock(event: BlockBreakEvent) {
        val (level) = event.player.getThisActiveEnchantmentOrNull<ExperienceEnchantment>() ?: return

        event.expToDrop = calculateDrops(event.expToDrop, level).toInt()
    }

    @EventHandler
    fun onFish(event: PlayerFishEvent) {
        if (event.state != PlayerFishEvent.State.CAUGHT_FISH) return
        val (level) = event.player.getThisActiveEnchantmentOrNull<ExperienceEnchantment>() ?: return

        event.expToDrop = calculateDrops(event.expToDrop, level).toInt()
    }

    private fun calculateDrops(xp: Int, level: Int): Double {
        return (xp * (level * ExperienceEnchantmentImpl.XP_BONUS_PERCENT_PER_LEVEL / 100.0)) + xp
    }
}

