package dev.slne.surf.enchantment.paper.utils

import dev.slne.surf.enchantment.api.utils.applyItemDamage
import dev.slne.surf.enchantment.paper.utils.events.FakeBlockBreakEvent
import org.bukkit.Effect
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import kotlin.time.Duration

object BlockBreakHandler {
    fun handleBlockBreak(
        blocks: List<Block>,
        cooldownHandler: CooldownHandler,
        cooldown: Duration,
        event: BlockBreakEvent,
        events: List<FakeBlockBreakEvent>,
        playBreakEffect: Boolean = false
    ) {
        val player = event.player
        val item = player.inventory.itemInMainHand

        if (blocks.isEmpty()) return

        if (player.gameMode != GameMode.CREATIVE) {
            cooldownHandler.applyCooldown(
                player,
                cooldown
            )

            item.applyItemDamage(blocks.size, player)
        }

        val drops = mutableListOf<ItemStack>()
        var totalExp = event.expToDrop

        for (breakEvent in events) {
            val block = breakEvent.block

            if (breakEvent.isCancelled || !blocks.contains(block)) continue

            drops.addAll(block.getDrops(item))
            totalExp += breakEvent.expToDrop

            if (playBreakEffect) {
                block.world.playEffect(block.location, Effect.STEP_SOUND, block.blockData)
            }

            block.type = Material.AIR
        }

        Dropper.handleDrops(player, drops, totalExp, event)
    }
}