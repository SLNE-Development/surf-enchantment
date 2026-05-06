@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.api.enchantment

import dev.slne.surf.api.core.rarity.Rarity
import dev.slne.surf.api.core.util.objectSetOf
import dev.slne.surf.api.paper.builder.LoreBuilder
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.tag.TagKey
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
import org.jetbrains.annotations.Range

abstract class AbstractCustomEnchantment(
    override val key: Key,
    override val displayName: Component,
    override val rarity: Rarity,
    override val description: LoreBuilder.(Int) -> Unit = {},
    override val supportedItems: TagKey<ItemType>? = null,
    override val primaryItems: TagKey<ItemType>? = null,
    override val weight: @Range(from = 1, to = 1024) Int? = 1,
    override val maxLevel: @Range(from = 1, to = 255) Int? = 1,

    /**
     * The enchantment cost determines the availability of enchantment levels in the Enchanting Table.
     * * The cost for a specific level is calculated as:
     * `base + (level - 1) * perLevelCost`
     *
     * - **minimumCost**: The threshold a table's "enchanting level" must reach for this level to appear.
     * - **maximumCost**: The threshold above which this enchantment level will no longer appear.
     *
     * ### Standard Table (Max Level 30) Configuration:
     * To limit the Enchanting Table to a specific maximum level while allowing higher levels
     * via Anvils (e.g., Table max Level 4, total max Level 5), use values like:
     * `minimumCost = of(9, 7)` -> Level 4 = 30 (Possible), Level 5 = 37 (Impossible in Table).
     */

    override val minimumCost: EnchantmentRegistryEntry.EnchantmentCost? = EnchantmentRegistryEntry.EnchantmentCost.of(
        0,
        0
    ),
    override val maximumCost: EnchantmentRegistryEntry.EnchantmentCost? = EnchantmentRegistryEntry.EnchantmentCost.of(
        0,
        0
    ),
    override val anvilCost: @Range(from = 1, to = Int.MAX_VALUE.toLong()) Int? = 1,
    override val activeSlots: Set<EquipmentSlotGroup> = objectSetOf(EquipmentSlotGroup.ANY),
    override val exclusiveWith: Set<Key>? = null,
    override val tags: Set<TagKey<Enchantment>>? = objectSetOf(),
    override val obtainableFromGameplay: Boolean = true,
    override val typedKey: TypedKey<Enchantment> = TypedKey.create(RegistryKey.ENCHANTMENT, key),
    override val listeners: Set<Listener> = objectSetOf(),
    override val jobs: Set<EnchantmentJob> = objectSetOf(),
) : CustomEnchantment {
    final override val bukkitEnchantment: Enchantment by lazy {
        RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(key)
    }

    final override val activeEquipmentSlots by lazy {
        EquipmentSlot.entries.filter { slot -> activeSlots.any { it.test(slot) } }
    }

    final override fun getThisEnchantmentOrNull(itemStack: ItemStack): Pair<Int, Enchantment>? {
        val enchantments = itemStack.enchantments
        val level = enchantments[bukkitEnchantment] ?: return null
        return Pair(level, bukkitEnchantment)
    }

    final override fun checkItemStackHasEnchantment(itemStack: ItemStack): Boolean =
        getThisEnchantmentOrNull(itemStack) != null

    final override fun getThisActiveEnchantmentOrNull(player: Player): Pair<Int, Enchantment>? {
        val equipment = player.equipment
        activeEquipmentSlots.forEach { slot ->
            val item = equipment.getItem(slot)
            val enchantment = getThisEnchantmentOrNull(item)
            if (enchantment != null) return enchantment
        }
        return null
    }

    final override fun hasEnchantmentActive(player: Player): Boolean {
        return getThisActiveEnchantmentOrNull(player) != null
    }

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CustomEnchantment) return false

        return key == other.key
    }

    final override fun hashCode(): Int {
        return key.hashCode()
    }
}