package com.songoda.fabledskyblock

import com.songoda.fabledskyblock.world.VoidGenerator
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.HandlerList
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin

class FabledSkyBlock : JavaPlugin() {

    companion object {
        lateinit var instance: FabledSkyBlock
            private set
    }

    override fun onEnable() {
        val console = Bukkit.getConsoleSender()
        console.sendMessage(formatText("&a============================="))
        console.sendMessage(formatText("&7${this.description.name} ${this.description.version} by &5${this.description.authors[0]} <3&7!"))
        console.sendMessage(formatText("&7Action: &aEnabling&7..."))
        console.sendMessage(formatText("&a============================="))

        instance = this
    }

    override fun onDisable() {
        val console = Bukkit.getConsoleSender()
        console.sendMessage(formatText("&a============================="))
        console.sendMessage(formatText("&7${this.description.name} ${this.description.version} by &5${this.description.authors[0]} <3&7!"))
        console.sendMessage(formatText("&7Action: &cDisabling&7..."))
        console.sendMessage(formatText("&a============================="))

        HandlerList.unregisterAll(this)
    }

    private fun formatText(input: String): String {
        return ChatColor.translateAlternateColorCodes('&', input)
    }

    override fun getDefaultWorldGenerator(worldName: String?, id: String?): ChunkGenerator = VoidGenerator()

}
