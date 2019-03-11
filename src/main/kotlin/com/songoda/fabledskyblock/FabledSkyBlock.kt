package com.songoda.fabledskyblock

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.event.HandlerList

class FabledSkyBlock : JavaPlugin() {

    companion object {
        var instance: FabledSkyBlock? = null
            private set
    }

    override fun onEnable() {
        val console = Bukkit.getConsoleSender()
        console.sendMessage(formatText("&a============================="))
        console.sendMessage(formatText("&7${this.description.name} " + this.description.version + " by &5${this.description.authors[0]} <3&7!"))
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

}