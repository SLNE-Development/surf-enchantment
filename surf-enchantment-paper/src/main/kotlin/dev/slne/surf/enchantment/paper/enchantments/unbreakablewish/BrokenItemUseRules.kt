@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.enchantments.unbreakablewish

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.entity.Leashable
import io.papermc.paper.entity.Shearable
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.entity.Armadillo
import org.bukkit.entity.CopperGolem
import org.bukkit.entity.Creeper
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.entity.SulfurCube
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * Right-click interactions that cost durability although they are handled by the
 * clicked block or entity rather than the item itself. A broken Unbreakable Wish item
 * must not perform them.
 *
 * Interactions of the item itself (hoe, flint and steel, bow, ...) are blocked
 * separately by denying the item use. Source: every `hurtAndBreak` call in vanilla 26.3.
 */
object BrokenItemUseRules {
    /** Durability a dispenser consumes per use of these items. */
    val DISPENSER_COST: Map<Material, Int> = mapOf(
        Material.SHEARS to 1,
        Material.FLINT_AND_STEEL to 1,
        Material.BRUSH to 16,
    )

    fun costsDurabilityOnBlock(item: ItemStack, block: Block): Boolean {
        val blockType = block.type
        return when {
            item.type == Material.SHEARS -> Tag.BEEHIVES.isTagged(blockType) || blockType == Material.PUMPKIN
            item.type == Material.FLINT_AND_STEEL -> blockType == Material.TNT
            Tag.ITEMS_DOUSES_CAMPFIRES.isTagged(item.type) && Tag.CAMPFIRES.isTagged(blockType) -> true
            Tag.ITEMS_AXES.isTagged(item.type) && Tag.COPPER_GOLEM_STATUES.isTagged(blockType) -> true
            else -> false
        }
    }

    fun costsDurabilityOnEntity(item: ItemStack, entity: Entity): Boolean = when (item.type) {
        Material.SHEARS -> entity is Shearable ||
                (entity is Leashable && entity.isLeashed) ||
                (entity is Mob && entity.wearsShearableEquipment())

        Material.BRUSH -> entity is Armadillo
        Material.FLINT_AND_STEEL -> entity is Creeper || entity is SulfurCube
        else -> Tag.ITEMS_AXES.isTagged(item.type) && entity is CopperGolem
    }

    private fun Mob.wearsShearableEquipment(): Boolean = EquipmentSlot.entries
        .filter(::canUseEquipmentSlot)
        .any { slot -> equipment.getItem(slot).getData(DataComponentTypes.EQUIPPABLE)?.canBeSheared() == true }
}
