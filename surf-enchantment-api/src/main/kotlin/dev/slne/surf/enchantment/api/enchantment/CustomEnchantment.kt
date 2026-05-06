@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.api.enchantment

import dev.slne.surf.enchantment.api.utils.Enchantable
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.tag.TagKey
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
import org.jetbrains.annotations.Range

interface CustomEnchantment : Enchantable {
    val supportedItems: TagKey<ItemType>?
    val primaryItems: TagKey<ItemType>?

    val weight: @Range(from = 1, to = 1024) Int?

    val minimumCost: EnchantmentRegistryEntry.EnchantmentCost?
    val maximumCost: EnchantmentRegistryEntry.EnchantmentCost?
    val anvilCost: @Range(from = 0, to = Int.MAX_VALUE.toLong()) Int?

    val activeSlots: Set<EquipmentSlotGroup>?
    val exclusiveWith: Set<Key>?
    val tags: Set<TagKey<Enchantment>>?

    /**
     * Controls whether this enchantment may be exposed through vanilla gameplay sources
     * such as enchanting tables, generated loot, mob equipment or trades.
     *
     * Event-only enchantments should set this to false and must not be assigned
     * gameplay source tags.
     */
    val obtainableFromGameplay: Boolean get() = true

    val typedKey: TypedKey<Enchantment>

    val bukkitEnchantment: Enchantment
    val activeEquipmentSlots: List<EquipmentSlot>

    val listeners: Set<Listener>
    val jobs: Set<EnchantmentJob>

    fun getThisEnchantmentOrNull(itemStack: ItemStack): Pair<Int, Enchantment>?
    fun checkItemStackHasEnchantment(itemStack: ItemStack): Boolean

    fun getThisActiveEnchantmentOrNull(player: Player): Pair<Int, Enchantment>?
    fun hasEnchantmentActive(player: Player): Boolean

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}