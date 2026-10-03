package dev.slne.surf.enchantment.paper.enchantment

import com.github.shynixn.mccoroutine.folia.*
import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.freeze
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.enchantment.api.enchantment.CustomEnchantment
import dev.slne.surf.enchantment.api.enchantment.EnchantmentManager
import dev.slne.surf.enchantment.api.enchantment.VanillaEnchantment
import dev.slne.surf.enchantment.api.enchantments.*
import dev.slne.surf.enchantment.api.enchantments.replenish.ReplenishEnchantment
import dev.slne.surf.enchantment.api.enchantments.telekinesis.TelekinesisEnchantment
import dev.slne.surf.enchantment.api.utils.Enchantable
import dev.slne.surf.enchantment.api.utils.InternalEnchantmentApi
import dev.slne.surf.enchantment.paper.plugin
import dev.slne.surf.enchantment.paper.utils.VanillaEnchantmentMap
import io.papermc.paper.registry.TypedKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import net.kyori.adventure.key.Key
import org.bukkit.Location
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

@AutoService(EnchantmentManager::class)
class EnchantmentManagerImpl : EnchantmentManager {
    private val frozen = AtomicBoolean(false)

    private val _customEnchantments = mutableObjectSetOf<CustomEnchantment>()
    override val customEnchantments = _customEnchantments.freeze()

    private val _vanillaEnchantments = mutableObjectSetOf<VanillaEnchantment>()
    override val vanillaEnchantments = _vanillaEnchantments.freeze()

    @InternalEnchantmentApi
    internal fun registerVanillaEnchantments() {
        VanillaEnchantmentMap.entries.forEach { entry ->
            _vanillaEnchantments.add(
                VanillaEnchantment(
                    entry.key,
                    entry.displayName,
                    entry.description,
                    entry.rarity,
                    entry.maxLevel
                )
            )
        }
    }

    @InternalEnchantmentApi
    internal fun registerCustomEnchantments() {
        register(TelekinesisEnchantment)
        register(ReplenishEnchantment)
        register(SoulboundEnchantment)
        register(SilentNightEnchantment)
        register(SilentGazeEnchantment)
        register(BeheadingEnchantment)
        register(RocketSaverEnchantment)
        register(RocketRideEnchantment)
        register(ExperienceEnchantment)
        //register(HoleDiggerEnchantment)
        //register(VeinMinerEnchantment)
        register(LumberJackEnchantment)
    }

    @InternalEnchantmentApi
    internal fun registerEnchantmentListeners() {
        _customEnchantments.forEach { enchantment ->
            enchantment.listeners.forEach { listener ->
                listener.register()
            }
        }
    }

    override fun registerCustomEnchantment(enchantment: CustomEnchantment) {
        _customEnchantments.add(enchantment)
    }

    override fun findCustomEnchantmentByKey(key: Key): CustomEnchantment? =
        _customEnchantments.firstOrNull { it.key == key }

    override fun findCustomEnchantment(clazz: KClass<out CustomEnchantment>) =
        _customEnchantments.firstOrNull { clazz.java.isAssignableFrom(it.javaClass) }

    override fun findVanillaEnchantmentByKey(key: Key) =
        _vanillaEnchantments.firstOrNull { it.key == key }

    override fun findVanillaEnchantmentByTypedKey(key: TypedKey<Enchantment>) =
        _vanillaEnchantments.firstOrNull { it.key == key }

    override fun findByBukkitEnchantment(enchantment: Enchantment): Enchantable? {
        val customEnchantment =
            _customEnchantments.firstOrNull { it.bukkitEnchantment == enchantment }
        if (customEnchantment != null) return customEnchantment

        return _vanillaEnchantments.firstOrNull { it.key == enchantment.key }
    }

    @InternalEnchantmentApi
    internal fun freeze() {
        frozen.set(true)
    }

    @InternalEnchantmentApi
    internal fun startEnchantmentJobs() {
        customEnchantments.forEach { enchantment ->
            enchantment.jobs.forEach { job ->
                job.start()
            }
        }
    }

    @InternalEnchantmentApi
    internal fun stopEnchantmentJobs() {
        customEnchantments.forEach { enchantment ->
            enchantment.jobs.forEach { job ->
                job.stop()
            }
        }
    }

    override val scope get() = plugin.scope
    override val globalRegionDispatcher get() = plugin.globalRegionDispatcher
    override val mainDispatcher get() = plugin.mainDispatcher
    override val asyncDispatcher get() = plugin.asyncDispatcher

    override val regionDispatcher: (Location) -> CoroutineContext
        get() = { location -> plugin.regionDispatcher(location) }

    override val entityDispatcher: (Entity) -> CoroutineContext
        get() = { entity -> plugin.entityDispatcher(entity) }

    override fun launch(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
    ): Job = plugin.launch(context, start, block)

    fun register(enchantment: CustomEnchantment) {
        require(!frozen.get()) { "Cannot register enchantments after the registry is frozen." }
        val added = _customEnchantments.add(enchantment)
        require(added) { "Enchantment with key ${enchantment.key} is already registered." }
    }
}

val enchantmentManagerImpl get() = EnchantmentManager.INSTANCE as EnchantmentManagerImpl