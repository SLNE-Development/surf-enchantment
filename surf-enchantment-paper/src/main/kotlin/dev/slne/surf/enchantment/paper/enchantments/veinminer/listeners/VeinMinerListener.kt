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
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import kotlin.time.Duration.Companion.seconds

object VeinMinerListener : Listener {
    private val blockHandler = BlockHandler()
    val cooldownHandler = CooldownHandler({
        appendSuccessPrefix()
        success("Vein Miner ist wieder bereit für den nächsten Schlag!")
    }, { secondsLeft ->
        appendErrorPrefix()
        error("Zu viele Erze auf einmal! Warte noch")
        appendSpace()
        variableValue("$secondsLeft Sekunden")
        error(".")
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBreak(event: BlockBreakEvent) {
        if (event is FakeBlockBreakEvent) return

        val player = event.player
        val item = player.inventory.itemInMainHand
        if (!item.hasCustomEnchantment<VeinMinerEnchantment>()) return

        val block = event.block
        if (block.type !in ORE_MATERIALS) return

        if (player.gameMode != GameMode.CREATIVE) {
            if (!cooldownHandler.checkCooldown(player.uniqueId)) return
        }

        val vein = VeinFinder.findVein(block)
        if (vein.size <= 1) return

        val blockResult = blockHandler.handleBlocks(player, vein.toList())
        val blocks = blockResult.breakableBlocks.take(VeinMinerEnchantmentImpl.MAX_ORES_TO_MINE)

        BlockBreakHandler.handleBlockBreak(
            cooldown = (blocks.size * VeinMinerEnchantmentImpl.COOLDOWN_SECONDS_PER_BLOCK).seconds,
            blocks = blocks,
            cooldownHandler = cooldownHandler,
            event = event,
            events = blockResult.events
        )

        VeinMinerAnimation.playVeinEffect(blocks)
    }
}
