package dev.slne.surf.enchantment.paper.utils

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

/**
 * Adds [item] to a matching, non-full off-hand stack first and the rest via [PlayerInventory.addItem].
 * An empty off-hand is never filled. Mutates [item] like `addItem` does.
 *
 * @return the items that could not be stored, in the same format as [PlayerInventory.addItem]
 */
fun PlayerInventory.addItemPreferringOffhand(item: ItemStack): Map<Int, ItemStack> {
    if (item.isEmpty) return emptyMap()

    val offhand = itemInOffHand
    if (offhand.isEmpty || !offhand.isSimilar(item)) return addItem(item)

    val space = minOf(offhand.maxStackSize, maxStackSize) - offhand.amount
    if (space <= 0) return addItem(item)

    val moved = minOf(space, item.amount)
    offhand.amount += moved
    setItemInOffHand(offhand)

    if (moved == item.amount) return emptyMap()
    item.amount -= moved
    return addItem(item)
}
