@file:Suppress("UnstableApiUsage", "HasPlatformType")

package dev.slne.surf.enchantment.paper.bootstrap

import dev.slne.surf.api.core.util.objectSetOf
import dev.slne.surf.enchantment.api.enchantment.CustomEnchantment
import dev.slne.surf.enchantment.api.enchantment.EnchantmentManager
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import io.papermc.paper.registry.tag.TagKey
import org.bukkit.enchantments.Enchantment

private val GAMEPLAY_SOURCE_TAGS = objectSetOf(
    EnchantmentTagKeys.IN_ENCHANTING_TABLE,
    EnchantmentTagKeys.ON_MOB_SPAWN_EQUIPMENT,
    EnchantmentTagKeys.ON_RANDOM_LOOT,
    EnchantmentTagKeys.TRADEABLE,
    EnchantmentTagKeys.TREASURE,
)

fun tagPostFlattenHandler() =
    LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT).newHandler { event ->
        val registrar = event.registrar()
        for (enchantment in EnchantmentManager.customEnchantments) {
            val tags = enchantment.tags ?: continue
            enchantment.validateGameplayTags(tags)

            for (tag in tags) {
                registrar.addToTag(tag, objectSetOf(enchantment.typedKey))
            }
        }
    }

private fun CustomEnchantment.validateGameplayTags(tags: Set<TagKey<Enchantment>>) {
    if (obtainableFromGameplay) return

    val blockedTags = tags.intersect(GAMEPLAY_SOURCE_TAGS)
    require(blockedTags.isEmpty()) {
        "Enchantment $key is marked as not obtainable from gameplay, but is assigned to gameplay source tags: $blockedTags"
    }
}
