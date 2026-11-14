@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.enchantments.unbreakablewish

import dev.slne.surf.enchantment.api.enchantments.SoulboundEnchantment
import dev.slne.surf.enchantment.api.enchantments.UnbreakableWishEnchantment
import dev.slne.surf.enchantment.paper.plugin
import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemEnchantments
import io.papermc.paper.persistence.PersistentDataContainerView
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Keeps the "broken" state of Unbreakable Wish items in sync.
 *
 * Invariant: the backup marker is present exactly when the item has Unbreakable Wish
 * and its damage has reached its maximum damage. While broken, the [SWAPPED] components
 * are removed (the item behaves like an empty hand) and all enchantments except the
 * ones that stay active are moved to `stored_enchantments`, where they have no effect.
 */
object BrokenItemState {
    val BACKUP_KEY = NamespacedKey("surf", "unbreakable_wish_backup")

    /**
     * Components that are removed while the item is broken and restored on repair.
     *
     * `minecraft:block_transformer` (hoe/axe/shovel right click) has no constant in
     * [DataComponentTypes], so it is looked up from the registry. It must never be cast
     * to a valued type.
     */
    private val SWAPPED: Set<DataComponentType> by lazy {
        val blockTransformer = RegistryAccess.registryAccess()
            .getRegistry(RegistryKey.DATA_COMPONENT_TYPE)
            .get(Key.key("minecraft", "block_transformer"))

        setOfNotNull(
            DataComponentTypes.ATTRIBUTE_MODIFIERS,
            DataComponentTypes.TOOL,
            DataComponentTypes.WEAPON,
            DataComponentTypes.BLOCKS_ATTACKS,
            DataComponentTypes.KINETIC_WEAPON,
            DataComponentTypes.PIERCING_WEAPON,
            DataComponentTypes.ATTACK_RANGE,
            DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,
            DataComponentTypes.STORED_ENCHANTMENTS,
            blockTransformer,
        )
    }

    fun hasWish(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty) return false
        return UnbreakableWishEnchantment.bukkitEnchantment in enchantmentsOf(item, DataComponentTypes.ENCHANTMENTS)
    }

    fun isBroken(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty) return false
        return isBroken(item.persistentDataContainer)
    }

    fun isBroken(pdc: PersistentDataContainerView): Boolean = pdc.has(BACKUP_KEY)

    fun needsSync(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty) return false
        return isBroken(item) != shouldBeBroken(item)
    }

    fun sync(item: ItemStack) {
        if (item.isEmpty) return

        val broken = isBroken(item)
        val shouldBeBroken = shouldBeBroken(item)

        when {
            shouldBeBroken && !broken -> applyBroken(item)
            broken && !shouldBeBroken -> restore(item)
        }
    }

    /**
     * Drops the dormant enchantments of a broken item. Only used by the grindstone,
     * which removes all non-curse enchantments, including the dormant ones.
     */
    fun discardDormantEnchantments(item: ItemStack) {
        item.unsetData(DataComponentTypes.STORED_ENCHANTMENTS)
    }

    private fun shouldBeBroken(item: ItemStack): Boolean {
        if (!hasWish(item)) return false
        if (item.hasData(DataComponentTypes.UNBREAKABLE)) return false

        val maxDamage = item.getData(DataComponentTypes.MAX_DAMAGE) ?: return false
        val damage = item.getData(DataComponentTypes.DAMAGE) ?: 0
        return damage >= maxDamage
    }

    private fun staysActive(enchantment: Enchantment): Boolean =
        enchantment == UnbreakableWishEnchantment.bukkitEnchantment ||
                enchantment == Enchantment.MENDING ||
                enchantment == SoulboundEnchantment.bukkitEnchantment ||
                enchantment.isCursed

    private fun applyBroken(item: ItemStack) {
        val backup = ItemStack.of(item.type)
        backup.copyDataFrom(item) { it in SWAPPED && item.isDataOverridden(it) }
        item.editPersistentDataContainer {
            it.set(BACKUP_KEY, PersistentDataType.BYTE_ARRAY, backup.serializeAsBytes())
        }

        val all = enchantmentsOf(item, DataComponentTypes.ENCHANTMENTS)
        val active = all.filterKeys(::staysActive)
        val dormant = all - active.keys

        SWAPPED.forEach(item::unsetData)
        item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false)
        item.setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(active))
        if (dormant.isNotEmpty()) {
            item.setData(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantments.itemEnchantments(dormant))
        }
    }

    private fun restore(item: ItemStack) {
        val bytes = item.persistentDataContainer.get(BACKUP_KEY, PersistentDataType.BYTE_ARRAY)

        // The active entry wins if an enchantment is present in both components.
        val merged = enchantmentsOf(item, DataComponentTypes.STORED_ENCHANTMENTS) +
                enchantmentsOf(item, DataComponentTypes.ENCHANTMENTS)
        item.setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(merged))

        SWAPPED.forEach(item::resetData)
        val backup = bytes?.let { runCatching { ItemStack.deserializeBytes(it) }.getOrNull() }
        if (backup != null) {
            item.copyDataFrom(backup) { it in SWAPPED && backup.isDataOverridden(it) }
        } else {
            plugin.logger.warning(
                "Unbreakable Wish backup of a ${item.type.key} could not be read, restoring default components"
            )
        }

        item.editPersistentDataContainer { it.remove(BACKUP_KEY) }
    }

    private fun enchantmentsOf(
        item: ItemStack,
        type: DataComponentType.Valued<ItemEnchantments>
    ): Map<Enchantment, Int> = item.getData(type)?.enchantments().orEmpty()
}
