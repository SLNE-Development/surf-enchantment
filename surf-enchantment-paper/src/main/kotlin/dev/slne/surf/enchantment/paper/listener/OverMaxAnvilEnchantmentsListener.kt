@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.listener

import dev.slne.surf.enchantment.paper.enchantments.unbreakablewish.BrokenItemState
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemEnchantments
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack

object OverMaxAnvilEnchantmentsListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAnvilPrepare(event: PrepareAnvilEvent) {
        val firstItem = event.inventory.firstItem ?: return
        val secondItem = event.inventory.secondItem ?: return

        if (firstItem.type.isAir || secondItem.type.isAir) {
            return
        }

        // Broken Unbreakable Wish items may only be repaired; BrokenItemRepairListener decides the result.
        if (BrokenItemState.isBroken(firstItem) || BrokenItemState.isBroken(secondItem)) {
            return
        }

        val firstEnchantments = firstItem.relevantEnchantments()
        val secondEnchantments = secondItem.relevantEnchantments()
        val highestInputLevels = highestLevels(firstEnchantments, secondEnchantments)

        if (!highestInputLevels.hasOverMax()) {
            return
        }

        val paperResult = event.result?.takeUnless { it.type.isAir }
        val usedFallbackResult = paperResult == null
        val result = paperResult?.clone()
            ?: createFallbackResult(firstItem, secondItem, secondEnchantments)
            ?: return

        val resultEnchantments = result.relevantEnchantments().toMutableMap()
        var changed = false

        for ((enchantment, highestInputLevel) in highestInputLevels) {
            if (highestInputLevel <= enchantment.maxLevel) {
                continue
            }

            val resultLevel = resultEnchantments[enchantment] ?: 0
            val mayAddMissing = resultLevel > 0 || mayAddMissingOverMax(
                enchantment = enchantment,
                firstItem = firstItem,
                secondItem = secondItem,
                result = result,
                firstEnchantments = firstEnchantments,
                secondEnchantments = secondEnchantments,
                resultEnchantments = resultEnchantments
            )

            if (!mayAddMissing || resultLevel == highestInputLevel) {
                continue
            }

            resultEnchantments[enchantment] = highestInputLevel
            changed = true
        }

        if (!changed) {
            return
        }

        result.setRelevantEnchantments(resultEnchantments)
        event.result = result
        updateAnvilCosts(event, usedFallbackResult)
    }

    private fun createFallbackResult(
        firstItem: ItemStack,
        secondItem: ItemStack,
        secondEnchantments: Map<Enchantment, Int>
    ): ItemStack? {
        if (!secondItem.isEnchantedBook || secondEnchantments.isEmpty()) {
            return null
        }

        if (firstItem.isEnchantedBook) {
            return firstItem.clone()
        }

        val firstEnchantments = firstItem.relevantEnchantments()
        val hasApplicableOverMaxEnchant = secondEnchantments.any { (enchantment, level) ->
            level > enchantment.maxLevel &&
                    enchantment.canEnchantItem(firstItem) &&
                    !enchantment.conflictsWithAny(firstEnchantments)
        }

        return if (hasApplicableOverMaxEnchant) firstItem.clone() else null
    }

    private fun mayAddMissingOverMax(
        enchantment: Enchantment,
        firstItem: ItemStack,
        secondItem: ItemStack,
        result: ItemStack,
        firstEnchantments: Map<Enchantment, Int>,
        secondEnchantments: Map<Enchantment, Int>,
        resultEnchantments: Map<Enchantment, Int>
    ): Boolean {
        if (enchantment.conflictsWithAny(resultEnchantments)) {
            return false
        }

        return when {
            result.isEnchantedBook -> firstItem.isEnchantedBook && secondItem.isEnchantedBook
            secondItem.isEnchantedBook -> secondEnchantments.isOverMax(enchantment) &&
                    enchantment.canEnchantItem(result)

            result.type != firstItem.type -> false
            !enchantment.canEnchantItem(result) -> false
            else -> firstEnchantments.isOverMax(enchantment) || secondEnchantments.isOverMax(enchantment)
        }
    }

    private fun highestLevels(
        firstEnchantments: Map<Enchantment, Int>,
        secondEnchantments: Map<Enchantment, Int>
    ): Map<Enchantment, Int> {
        return firstEnchantments.toMutableMap().apply {
            secondEnchantments.forEach { (enchantment, level) ->
                merge(enchantment, level, ::maxOf)
            }
        }
    }

    private fun ItemStack.relevantEnchantments(): Map<Enchantment, Int> {
        return if (isEnchantedBook) storedEnchantments() else itemEnchantments()
    }

    private fun ItemStack.storedEnchantments(): Map<Enchantment, Int> {
        return getData(DataComponentTypes.STORED_ENCHANTMENTS)
            ?.enchantments()
            ?: emptyMap()
    }

    private fun ItemStack.itemEnchantments(): Map<Enchantment, Int> {
        return getData(DataComponentTypes.ENCHANTMENTS)
            ?.enchantments()
            ?: itemMeta?.enchants
            ?: emptyMap()
    }

    private fun ItemStack.setRelevantEnchantments(enchantments: Map<Enchantment, Int>) {
        val component = ItemEnchantments.itemEnchantments(enchantments)

        if (isEnchantedBook) {
            setData(DataComponentTypes.STORED_ENCHANTMENTS, component)
        } else {
            setData(DataComponentTypes.ENCHANTMENTS, component)
        }
    }

    private fun Enchantment.conflictsWithAny(enchantments: Map<Enchantment, Int>): Boolean {
        return enchantments.keys.any { existing ->
            existing != this && (existing.conflictsWith(this) || conflictsWith(existing))
        }
    }

    private fun Map<Enchantment, Int>.hasOverMax(): Boolean {
        return any { (enchantment, level) -> level > enchantment.maxLevel }
    }

    private fun Map<Enchantment, Int>.isOverMax(enchantment: Enchantment): Boolean {
        return getOrDefault(enchantment, 0) > enchantment.maxLevel
    }

    private val ItemStack.isEnchantedBook get() = type == Material.ENCHANTED_BOOK

    private fun updateAnvilCosts(
        event: PrepareAnvilEvent,
        usedFallbackResult: Boolean
    ) {
        val view = event.view

        // Do not recalculate costs here. Paper already calculated the vanilla/anvil
        // cost, including correct rename behavior. Only keep custom fallback results enabled.
        if (view.repairCost < 1) {
            view.repairCost = 1
        }

        if (usedFallbackResult && view.repairItemCountCost < 1) {
            view.repairItemCountCost = 1
        }

        if (view.maximumRepairCost <= view.repairCost) {
            view.maximumRepairCost =
                if (view.repairCost == Int.MAX_VALUE) Int.MAX_VALUE else view.repairCost + 1
        }
    }
}
