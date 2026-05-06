package dev.slne.surf.enchantment.paper.utils

import dev.slne.surf.enchantment.api.enchantments.telekinesis.PostTelekinesisItemEvent
import dev.slne.surf.enchantment.api.enchantments.telekinesis.PreTelekinesisItemEvent
import dev.slne.surf.enchantment.api.enchantments.telekinesis.TelekinesisEnchantment
import dev.slne.surf.enchantment.api.utils.hasCustomEnchantment
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

object Dropper {
    fun handleDrops(
        player: Player,
        drops: List<ItemStack>,
        totalExp: Int,
        event: BlockBreakEvent
    ) {
        val location = event.block.location.clone().add(0.5, 0.5, 0.5)
        val item = event.player.inventory.itemInMainHand
        val telekinesis = item.hasCustomEnchantment<TelekinesisEnchantment>()

        if (telekinesis) {
            handleTelekinesis(player, drops, totalExp, event)
        } else {
            handleNormalDrops(location, drops, totalExp, event)
        }
    }

    private fun handleTelekinesis(
        player: Player,
        drops: List<ItemStack>,
        totalExp: Int,
        event: BlockBreakEvent
    ) {
        val preEvent = PreTelekinesisItemEvent(
            player = player,
            itemStacks = drops.toMutableList()
        )

        if (!preEvent.callEvent()) {
            return
        }

        val modifiedDrops = preEvent.itemStacks

        modifiedDrops.forEach { drop ->
            val rest = player.inventory.addItem(drop)

            val postTelekinesisItemEvent = PostTelekinesisItemEvent(
                player = player,
                itemStack = drop,
                notAddedToInventory = rest.toMap(),
                originEvent = event
            )

            postTelekinesisItemEvent.callEvent()

            val finalRest = postTelekinesisItemEvent.notAddedToInventory.values.toList()

            if (finalRest.isNotEmpty()) {
                drop(player.location, finalRest)
            }
        }

        player.giveExp(totalExp, true)
        event.expToDrop = 0
    }

    private fun handleNormalDrops(
        location: Location,
        drops: List<ItemStack>,
        totalExp: Int,
        event: BlockBreakEvent
    ) {
        drop(location, drops)

        event.expToDrop = totalExp
    }

    private fun drop(location: Location, drops: List<ItemStack>) {
        val stacked = stackDrops(drops)

        stacked.forEach { drop ->
            location.world.dropItem(location, drop)
        }
    }

    private fun stackDrops(drops: List<ItemStack>): List<ItemStack> {
        val result = mutableListOf<ItemStack>()

        for (drop in drops) {
            var remaining = drop.amount

            for (existing in result) {
                if (!existing.isSimilar(drop)) continue

                val space = existing.maxStackSize - existing.amount
                val toAdd = minOf(space, remaining)

                existing.amount += toAdd
                remaining -= toAdd

                if (remaining <= 0) break
            }

            while (remaining > 0) {
                val clone = drop.clone()
                val amount = minOf(clone.maxStackSize, remaining)
                clone.amount = amount
                result.add(clone)
                remaining -= amount
            }
        }

        return result
    }
}