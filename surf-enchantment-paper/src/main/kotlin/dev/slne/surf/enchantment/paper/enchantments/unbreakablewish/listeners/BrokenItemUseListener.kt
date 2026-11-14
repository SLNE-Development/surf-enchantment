@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.enchantments.unbreakablewish.listeners

import dev.slne.surf.enchantment.paper.enchantments.unbreakablewish.BrokenItemState
import dev.slne.surf.enchantment.paper.enchantments.unbreakablewish.BrokenItemUseRules
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

object BrokenItemUseListener : Listener {
    /** Fall distance from which vanilla turns a mace hit into a smash attack. */
    private const val MACE_SMASH_FALL_DISTANCE = 1.5f

    /**
     * Not `ignoreCancelled`: right clicks into the air arrive already cancelled, but the
     * item use (bow, fishing rod, ...) still happens unless it is denied explicitly.
     *
     * Only the item use is denied, so doors, chests and buttons keep working. Equippable
     * items keep their right click because equipping costs no durability.
     */
    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return

        val item = event.item ?: return
        if (!BrokenItemState.isBroken(item)) return

        if (!item.hasData(DataComponentTypes.EQUIPPABLE)) {
            event.setUseItemInHand(Event.Result.DENY)
        }

        val block = event.clickedBlock ?: return
        if (BrokenItemUseRules.costsDurabilityOnBlock(item, block)) {
            event.setUseInteractedBlock(Event.Result.DENY)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        val item = event.player.inventory.getItem(event.hand)
        if (!BrokenItemState.isBroken(item)) return

        if (BrokenItemUseRules.costsDurabilityOnEntity(item, event.rightClicked)) {
            event.isCancelled = true
        }
    }

    /** The mace smash bonus is item-class behavior and cannot be removed via components. */
    @EventHandler(ignoreCancelled = true)
    fun onAttack(event: PrePlayerAttackEntityEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand
        if (item.type != Material.MACE || !BrokenItemState.isBroken(item)) return

        if (player.fallDistance > MACE_SMASH_FALL_DISTANCE && !player.isGliding) {
            event.isCancelled = true
        }
    }

    /**
     * Dispensers damage items without a player, so no damage or break event fires.
     * Droppers only eject items, which costs no durability, and must keep working.
     */
    @EventHandler(ignoreCancelled = true)
    fun onDispense(event: BlockDispenseEvent) {
        if (event.block.type != Material.DISPENSER) return

        val item = event.item
        val cost = BrokenItemUseRules.DISPENSER_COST[item.type] ?: return
        if (!BrokenItemState.hasWish(item)) return

        val maxDamage = item.getData(DataComponentTypes.MAX_DAMAGE) ?: return
        val damage = item.getData(DataComponentTypes.DAMAGE) ?: 0
        if (BrokenItemState.isBroken(item) || maxDamage - damage <= cost) {
            event.isCancelled = true
        }
    }
}
