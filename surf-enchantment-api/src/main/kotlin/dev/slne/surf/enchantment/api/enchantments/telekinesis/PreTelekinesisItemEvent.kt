package dev.slne.surf.enchantment.api.enchantments.telekinesis

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

/**
 * Called before items are added to the player's inventory by Telekinesis.
 *
 * This event allows you to modify the list of items that will be added to the player's inventory
 * when Telekinesis is triggered. You can add, remove, or modify the items in the list as needed.
 *
 * Cancelling this event will prevent any items from being added to the player's inventory by Telekinesis.
 * Note that cancelling this event does not prevent the original item drop or experience drop from occurring;
 * it only prevents the items from being added to the player's inventory. The items will still drop in the world as normal.
 */
class PreTelekinesisItemEvent(
    val player: Player,
    var itemStacks: MutableList<ItemStack>
) : Event(), Cancellable {
    private var cancelled = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    override fun getHandlers(): HandlerList {
        return HANDLER_LIST
    }

    companion object {
        private val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLER_LIST
        }
    }
}