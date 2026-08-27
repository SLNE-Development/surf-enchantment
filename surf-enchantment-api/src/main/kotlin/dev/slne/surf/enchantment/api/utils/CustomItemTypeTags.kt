@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.api.utils

import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.util.objectSetOf
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.keys.ItemTypeKeys
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys
import io.papermc.paper.registry.tag.TagKey
import io.papermc.paper.tag.TagEntry
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemType

enum class CustomItemTypeTags(
    key: Key,
    vararg tags: TagEntry<ItemType>
) {

    BEHEADING_KEY(
        key("surf", "items_beheading"),
        TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_WEAPON),
        TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_TRIDENT),
        TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_BOW),
        TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_CROSSBOW),
    ),

    EXPERIENCE_KEY(
        key("surf", "items_experience"),
        TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_WEAPON),
        TagEntry.tagEntry(ItemTypeTagKeys.PICKAXES),
        TagEntry.tagEntry(ItemTypeTagKeys.AXES),
        TagEntry.tagEntry(ItemTypeTagKeys.HOES),
        TagEntry.valueEntry(ItemTypeKeys.FISHING_ROD),
        TagEntry.valueEntry(ItemTypeKeys.TRIDENT)
    ),

    HOLE_DIGGER_KEY(
        key("surf", "items_hole_digger"),
        TagEntry.tagEntry(ItemTypeTagKeys.PICKAXES)
    ),

    LUMBERJACK_KEY(
        key("surf", "items_lumberjack"),
        TagEntry.tagEntry(ItemTypeTagKeys.AXES)
    ),

    REPLENISH_KEY(
        key("surf", "items_replenish"),
        TagEntry.tagEntry(ItemTypeTagKeys.HOES)
    ),

    ROCKET_RIDE_KEY(
        key("surf", "items_rocket_ride"),
        TagEntry.tagEntry(ItemTypeTagKeys.HARNESSES)
    ),

    ROCKET_SAVER_KEY(
        key("surf", "items_rocket_saver"),
        TagEntry.valueEntry(ItemTypeKeys.ELYTRA)
    ),

    SILENT_GAZE_KEY(
        key("surf", "items_silent_gaze"),
        TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_HEAD_ARMOR)
    ),

    SILENT_NIGHT_KEY(
        key("surf", "items_silent_night"),
        TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_HEAD_ARMOR)
    ),

    SOULBOUND_KEY(
        key("surf", "items_soulbound"),
        TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_DURABILITY),
        TagEntry.tagEntry(ItemTypeTagKeys.BUNDLES)
    ),

    TELEKINESIS_KEY(
        key("surf", "items_telekinesis"),
        TagEntry.valueEntry(ItemTypeKeys.SHEARS),
        TagEntry.tagEntry(ItemTypeTagKeys.AXES),
        TagEntry.tagEntry(ItemTypeTagKeys.HOES),
        TagEntry.tagEntry(ItemTypeTagKeys.PICKAXES),
        TagEntry.tagEntry(ItemTypeTagKeys.SHOVELS),
        TagEntry.tagEntry(ItemTypeTagKeys.SWORDS),
        TagEntry.tagEntry(ItemTypeTagKeys.SPEARS),
        TagEntry.valueEntry(ItemTypeKeys.BOW),
        TagEntry.valueEntry(ItemTypeKeys.CROSSBOW),
        TagEntry.valueEntry(ItemTypeKeys.TRIDENT),
        TagEntry.valueEntry(ItemTypeKeys.MACE)
    ),

    VEIN_MINER_KEY(
        key("surf", "items_vein_miner"),
        TagEntry.tagEntry(ItemTypeTagKeys.PICKAXES)
    );

    val tagKey = TagKey.create(RegistryKey.ITEM, key)
    val tagEntries: ObjectSet<TagEntry<ItemType>> = objectSetOf(*tags)
}