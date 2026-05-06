package dev.slne.surf.enchantment.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.api.paper.packet.SurfPaperPacketApi
import dev.slne.surf.enchantment.paper.commands.surfEnchantmentCommand
import dev.slne.surf.enchantment.paper.enchantment.enchantmentManagerImpl
import dev.slne.surf.enchantment.paper.listener.GameplayObtainabilityListener
import dev.slne.surf.enchantment.paper.listener.IllegalAnvilEnchantmentsListener
import dev.slne.surf.enchantment.paper.lore.SurfEnchantmentPacketLoreHandler
import org.bukkit.plugin.java.JavaPlugin

class SurfEnchantment : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        enchantmentManagerImpl.freeze()
        enchantmentManagerImpl.registerVanillaEnchantments()
    }

    override suspend fun onEnableAsync() {
        enchantmentManagerImpl.registerEnchantmentListeners()
        SurfPaperPacketApi.INSTANCE.registerPacketLoreListenerGlobal(
            this,
            SurfEnchantmentPacketLoreHandler
        )
        enchantmentManagerImpl.startEnchantmentJobs()

        surfEnchantmentCommand()

        IllegalAnvilEnchantmentsListener.register()
        GameplayObtainabilityListener.register()
    }

    override suspend fun onDisableAsync() {
        enchantmentManagerImpl.stopEnchantmentJobs()
    }
}

val plugin get() = JavaPlugin.getPlugin(SurfEnchantment::class.java)