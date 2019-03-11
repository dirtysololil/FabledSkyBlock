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

        var test = 1
        val test2 = 2

        Bukkit.broadcastMessage("We in!")
    }

    override fun onDisable() {
        Bukkit.broadcastMessage("We out!")
    }

}