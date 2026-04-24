package dev.slne.surf.enchantment.paper.enchantments.veinminer

import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.enchantment.paper.plugin
import kotlinx.coroutines.delay
import net.kyori.adventure.sound.Sound
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.Sound as BukkitSound

object VeinMinerAnimation {
    fun playVeinEffect(blocks: List<Block>) {
        if (blocks.isEmpty()) return

        val firstLocation = blocks.first().location.clone().toCenterLocation()
        val nearbyPlayers = firstLocation.getNearbyPlayers(32.0)
        if (nearbyPlayers.isEmpty()) return

        plugin.launch {
            blocks.forEachIndexed { index, block ->
                val loc = block.location.toCenterLocation()
                val data = block.blockData

                nearbyPlayers.forEach { player ->
                    player.playSound(true) {
                        type(BukkitSound.BLOCK_MEDIUM_AMETHYST_BUD_BREAK)
                        source(Sound.Source.NEUTRAL)
                        pitch(0.5f + (index * 0.1f).coerceAtMost(1.5f))
                    }

                    player.spawnParticle(
                        Particle.BLOCK,
                        loc,
                        15,
                        0.15,
                        0.15,
                        0.15,
                        0.05,
                        data
                    )
                    player.spawnParticle(
                        Particle.SCRAPE,
                        loc,
                        3,
                        0.1,
                        0.1,
                        0.1,
                        0.01
                    )

                    if (index < blocks.size - 1) {
                        val nextLoc = blocks[index + 1].location.toCenterLocation()
                        drawEnergyLine(player, loc, nextLoc)
                    }
                }

                delay(5.ticks)
            }
        }
    }

    private fun drawEnergyLine(player: Player, from: Location, to: Location) {
        val distance = from.distance(to)
        val points = (distance * 2).toInt().coerceAtLeast(1)
        val vector = to.toVector().subtract(from.toVector()).multiply(1.0 / points)

        val current = from.clone()
        for (i in 0 until points) {
            current.add(vector)
            player.spawnParticle(
                Particle.ELECTRIC_SPARK,
                current,
                1,
                0.0,
                0.0,
                0.0,
                0.0
            )
        }
    }
}