package com.songoda.fabledskyblock

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin


class FabledSkyBlock : JavaPlugin() {

    companion object {
        var instance: FabledSkyBlock? = null
        private set
    }

    override fun onEnable() {
        instance = this

        Bukkit.broadcastMessage("Enabled test")
    }

    override fun onDisable() {
        Bukkit.broadcastMessage("Disabled test")
    }

}