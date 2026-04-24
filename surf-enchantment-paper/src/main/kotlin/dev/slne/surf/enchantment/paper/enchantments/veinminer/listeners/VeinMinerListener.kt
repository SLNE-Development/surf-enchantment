package dev.slne.surf.enchantment.paper.enchantments.veinminer.listeners

import dev.slne.surf.enchantment.api.enchantments.VeinMinerEnchantment
import dev.slne.surf.enchantment.api.utils.hasCustomEnchantment
import dev.slne.surf.enchantment.paper.enchantments.veinminer.VeinFinder
import dev.slne.surf.enchantment.paper.enchantments.veinminer.VeinMinerAnimation
import dev.slne.surf.enchantment.paper.enchantments.veinminer.VeinMinerEnchantmentImpl
import dev.slne.surf.enchantment.paper.utils.BlockBreakHandler
import dev.slne.surf.enchantment.paper.utils.BlockHandler
import dev.slne.surf.enchantment.paper.utils.CooldownHandler
import dev.slne.surf.enchantment.paper.utils.events.FakeBlockBreakEvent
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import kotlin.time.Duration.Companion.milliseconds

object VeinMinerListener : Listener {
    private val blockHandler = BlockHandler()
    val cooldownHandler = CooldownHandler({
        appendSuccessPrefix()
        success("Deine Ausdauer ist zurückgekehrt – bereit für die nächste Ader!")
    }, { secondsLeft ->
        appendErrorPrefix()
        error("Deine Arme sind noch schwer vom letzten Abbau!")
    })

    private val ORE_MATERIALS = setOf(
        Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
        Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
        Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
        Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
        Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
        Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
        Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
        Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
        Material.NETHER_QUARTZ_ORE, Material.NETHER_GOLD_ORE,
        Material.ANCIENT_DEBRIS
    )

    val Block.isOre: Boolean get() = ORE_MATERIALS.contains(this.type)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBreak(event: BlockBreakEvent) {
        if (event is FakeBlockBreakEvent) return

        val player = event.player
        val item = player.inventory.itemInMainHand
        if (!item.hasCustomEnchantment<VeinMinerEnchantment>()) return

        val block = event.block
        if (!block.isOre) return

        if (player.gameMode != GameMode.CREATIVE) {
            if (!cooldownHandler.checkCooldown(player.uniqueId)) return
        }

        val veinBlocks = VeinFinder.findVein(block)
        if (veinBlocks.size <= 1) return

        val blockResult = blockHandler.handleBlocks(player, veinBlocks.toList())
        val blocksToMine = blockResult.breakableBlocks.take(VeinMinerEnchantmentImpl.MAX_ORES_TO_MINE)

        BlockBreakHandler.handleBlockBreak(
            cooldown = (blocksToMine.size * VeinMinerEnchantmentImpl.COOLDOWN_PER_ORE_MS).milliseconds,
            blocks = blocksToMine,
            cooldownHandler = cooldownHandler,
            event = event,
            events = blockResult.events
        )

        VeinMinerAnimation.playVeinEffect(blocksToMine)
    }
}