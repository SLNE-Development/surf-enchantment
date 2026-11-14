@file:Suppress("UnstableApiUsage")

package dev.slne.surf.enchantment.paper.enchantments.unbreakablewish

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.api.core.rarity.Rarity
import dev.slne.surf.enchantment.api.enchantment.AbstractCustomEnchantment
import dev.slne.surf.enchantment.api.enchantments.UnbreakableWishEnchantment
import dev.slne.surf.enchantment.api.utils.CustomItemTypeTags
import dev.slne.surf.enchantment.paper.enchantments.unbreakablewish.listeners.BrokenItemStateListener
import dev.slne.surf.enchantment.paper.enchantments.unbreakablewish.listeners.BrokenItemUseListener
import io.papermc.paper.registry.data.EnchantmentRegistryEntry

@AutoService(UnbreakableWishEnchantment::class)
class UnbreakableWishEnchantmentImpl : AbstractCustomEnchantment(
    key = key("surf", "unbreakable_wish"),
    displayName = text("Unbreakable Wish"),
    rarity = Rarity.MYTHIC,
    description = {
        line {
            darkSpacer("Verhindert, dass dein Werkzeug durch")
        }
        line {
            darkSpacer("Haltbarkeitsverlust zerbricht")
        }
    },
    supportedItems = CustomItemTypeTags.UNBREAKABLE_WISH_KEY.tagKey,
    weight = 2,
    minimumCost = EnchantmentRegistryEntry.EnchantmentCost.of(
        15,
        9
    ),
    maximumCost = EnchantmentRegistryEntry.EnchantmentCost.of(
        65,
        9
    ),
    maxLevel = 1,
    obtainableFromGameplay = false,
    listeners = setOf(BrokenItemStateListener, BrokenItemUseListener),
), UnbreakableWishEnchantment
