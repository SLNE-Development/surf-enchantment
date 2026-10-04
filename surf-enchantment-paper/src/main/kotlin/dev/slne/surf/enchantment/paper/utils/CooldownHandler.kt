package dev.slne.surf.enchantment.paper.utils

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.enchantment.api.enchantment.EnchantmentJob
import dev.slne.surf.enchantment.api.enchantment.EnchantmentManager.Companion.launch
import dev.slne.surf.enchantment.paper.plugin
import kotlinx.coroutines.CoroutineScope
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.time.OffsetDateTime
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val lumberJackReductionKey =
    NamespacedKey("surf-skill-paper", "lumberjack_cooldown_reduction")

class CooldownHandler(
    private val expirationMessage: (SurfComponentBuilder.() -> Unit)? = null,
    private val notReadyMessage: SurfComponentBuilder.(Long) -> Unit = {},
    private val defaultCooldown: Duration = 5.minutes,
    private val allowReduction: Boolean = false,
    lastMessageExpiration: Duration = 3.seconds
) : EnchantmentJob() {
    private val expirationListeners = mutableListOf<suspend CoroutineScope.(UUID) -> Unit>()

    private val lastMessage = Caffeine.newBuilder()
        .expireAfterWrite(lastMessageExpiration)
        .maximumSize(10_000)
        .build<UUID, Unit>()

    private val cooldowns = Caffeine.newBuilder()
        .maximumSize(10_000)
        .build<UUID, OffsetDateTime>()

    override suspend fun tick() {
        val now = OffsetDateTime.now()

        cooldowns.asMap().entries.removeIf { (uuid, expireTime) ->
            if (expireTime.isAfter(now)) return@removeIf false

            expirationMessage?.let { server.getPlayer(uuid)?.sendText(it) }

            expirationListeners.forEach { listener ->
                launch(plugin.globalRegionDispatcher) {
                    listener(uuid)
                }
            }
            true
        }
    }

    fun checkCooldown(uuid: UUID, receiverPlayer: Player? = null): Boolean {
        val expireTime = cooldowns.getIfPresent(uuid) ?: return true

        val remaining = expireTime.toEpochSecond() - OffsetDateTime.now().toEpochSecond()

        if (remaining <= 0) {
            cooldowns.invalidate(uuid)
            return true
        }

        if (lastMessage.getIfPresent(uuid) == null) {
            val player = receiverPlayer ?: server.getPlayer(uuid)

            player?.sendText {
                notReadyMessage(this, remaining)
            }

            lastMessage.put(uuid, Unit)
        }

        return false
    }

    fun applyCooldown(
        player: Player,
        cooldown: Duration = defaultCooldown,
        uuid: UUID = player.uniqueId
    ) {

        if (!allowReduction) {
            cooldowns.put(
                uuid,
                OffsetDateTime.now().plusSeconds(cooldown.inWholeSeconds)
            )
            return
        }

        val reductionSeconds = player.persistentDataContainer.get(
            lumberJackReductionKey,
            PersistentDataType.DOUBLE
        ) ?: 0.0

        val finalCooldown = cooldown
            .minus(reductionSeconds.seconds)
            .coerceAtLeast(0.seconds)

        cooldowns.put(
            uuid,
            OffsetDateTime.now().plusSeconds(finalCooldown.inWholeSeconds)
        )
    }

    fun invalidateCooldown(uuid: UUID) {
        cooldowns.invalidate(uuid)
    }

    fun registerExpirationListener(listener: suspend CoroutineScope.(UUID) -> Unit) {
        expirationListeners.add(listener)
    }
}