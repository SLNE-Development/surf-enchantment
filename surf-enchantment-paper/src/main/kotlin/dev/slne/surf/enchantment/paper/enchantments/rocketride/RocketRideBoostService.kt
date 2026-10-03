package dev.slne.surf.enchantment.paper.enchantments.rocketride

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.enchantment.paper.plugin
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.GameMode
import org.bukkit.entity.HappyGhast
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object RocketRideBoostService {
    private const val END_VELOCITY_FACTOR = 0.35

    private val activeBoosts = ConcurrentHashMap<UUID, Job>()

    fun isBoosting(ghast: HappyGhast): Boolean {
        return activeBoosts[ghast.uniqueId]?.isActive == true
    }

    fun startBoost(
        ghast: HappyGhast,
        rider: Player,
        power: Double,
        upward: Double,
        durationTicks: Int
    ) {

        if (rider.gameMode == GameMode.SPECTATOR) return

        activeBoosts[ghast.uniqueId]?.cancel()

        val job = plugin.launch {
            withContext(plugin.entityDispatcher(ghast)) {
                try {
                    runBoost(ghast, rider, power, upward, durationTicks)
                } finally {
                    activeBoosts.remove(ghast.uniqueId)
                }
            }
        }

        activeBoosts[ghast.uniqueId] = job
    }

    private suspend fun runBoost(
        ghast: HappyGhast,
        rider: Player,
        power: Double,
        upward: Double,
        totalTicks: Int
    ) {

        val boostTicks = (totalTicks * 0.75).toInt()
        val slowTicks = totalTicks - boostTicks

        val look = rider.location.direction.normalize()
        val startVelocity = ghast.velocity.clone()
        var currentVelocity = startVelocity.clone()

        var tick = 0
        while (tick < boostTicks && ghast.isValid && !ghast.isDead) {

            val progress = tick.toDouble() / boostTicks
            val multiplier = sin(progress * (PI / 2))

            val boostVector = Vector(
                look.x * power * multiplier,
                look.y * power * multiplier + upward * multiplier,
                look.z * power * multiplier
            )

            currentVelocity = startVelocity.clone().add(boostVector)

            ghast.velocity = currentVelocity
            rider.fallDistance = 0f

            sendProgressBar(ghast, tick, totalTicks)

            tick++
            delay(1.ticks)
        }

        val peakVelocity = currentVelocity.clone()
        val endVelocity = peakVelocity.clone().multiply(END_VELOCITY_FACTOR)

        var slowTick = 0
        while (slowTick < slowTicks && ghast.isValid && !ghast.isDead) {

            val progress = slowTick.toDouble() / slowTicks
            val multiplier = cos(progress * (PI / 2))

            val interpolated = peakVelocity.clone().multiply(multiplier)
                .add(endVelocity.clone().multiply(1 - multiplier))

            ghast.velocity = interpolated
            rider.fallDistance = 0f

            sendProgressBar(ghast, boostTicks + slowTick, totalTicks)

            slowTick++
            delay(1.ticks)
        }

        // Velocity is no longer overridden from here on, so the remaining momentum
        // decays naturally through vanilla drag instead of stopping abruptly.
        sendProgressBar(ghast, boostTicks + slowTick, totalTicks)
    }

    fun stopBoost(ghast: HappyGhast) {
        activeBoosts[ghast.uniqueId]?.cancel()
        activeBoosts.remove(ghast.uniqueId)
    }

    private fun sendProgressBar(ghast: HappyGhast, current: Int, total: Int) {
        val barLength = 20
        val percent = current.toDouble() / total
        val reachedBars = (percent * barLength).toInt().coerceIn(0, barLength)

        val bar = buildText {
            success("|".repeat(reachedBars))
            spacer("|".repeat(barLength - reachedBars))
        }

        ghast.passengers.filterIsInstance<Player>().forEach { passenger ->
            passenger.sendActionBar(buildText {
                append(bar)
            })
        }
    }
}