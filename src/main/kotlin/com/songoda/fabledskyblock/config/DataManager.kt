package com.songoda.fabledskyblock.config

import com.songoda.fabledskyblock.FabledSkyBlock
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.*
import java.io.IOException
import java.nio.charset.Charset
import java.io.ByteArrayInputStream
import java.io.BufferedReader


object DataManager {

    enum class DataFile(val fileName: String) {
        CONFIG("config.yml"),
        WORLDS("worlds.yml"),
        LEVELLING("levelling.yml"),
        LANGUAGE("language.yml"),
        SETTINGS("settings.yml"),
        UPGRADES("upgrades.yml"),
        GENERATORS("generators.yml"),
        STACKABLES("stackables.yml"),
        STRUCTURES("structures.yml");

        val file: File = File(FabledSkyBlock.instance.dataFolder.toString() + fileName)
        lateinit var fileConfiguration: FileConfiguration

        fun reload() {
            if (fileName == "config.yml") {
                this.fileConfiguration = YamlConfiguration.loadConfiguration(InputStreamReader(DataManager.getConfigContent(this.file)))
            } else {
                this.fileConfiguration = YamlConfiguration.loadConfiguration(this.file)
            }
        }
    }

    init {
        reloadAll()
    }

    fun reloadAll() {
        val dataFolder = FabledSkyBlock.instance.dataFolder

        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }

        if (!File(dataFolder.toString() + "/structures").exists()) {
            File(dataFolder.toString() + "/structures").mkdir()
        }

        for (dataFile in DataFile.values()) {
            dataFile.reload()
        }

        // TODO: Create/Update files, probably just rewrite the entire file loading/updating system
        // https://gitlab.com/Songoda/SkyBlock/blob/master/src/main/java/me/goodandevil/skyblock/config/FileManager.java
    }

    private fun getConfigContent(reader: Reader): InputStream? {
        try {
            var addLine: String
            var currentLine: String
            val pluginName = FabledSkyBlock.instance.description.name
            var commentNum = 0

            val whole = StringBuilder("")
            val bufferedReader = BufferedReader(reader)
            val iterator = bufferedReader.lineSequence().iterator()

            while (iterator.hasNext()) {
                currentLine = iterator.next()
                if (currentLine.contains("#")) {
                    addLine = currentLine.replace("[!]", "IMPORTANT").replace(":", "-").replaceFirst("#".toRegex(), "${pluginName}_COMMENT_$commentNum:")
                    whole.append(addLine + "\n")
                    commentNum++
                } else {
                    whole.append(currentLine + "\n")
                }
            }

            bufferedReader.close()

            return ByteArrayInputStream(whole.toString().toByteArray(Charset.forName("UTF-8")))
        } catch (e: IOException) {
            e.printStackTrace()

            return null
        }
    }

    private fun getConfigContent(configFile: File): InputStream? {
        if (!configFile.exists()) {
            return null
        }

        try {
            return getConfigContent(FileReader(configFile))
        } catch (ex: FileNotFoundException) {
            ex.printStackTrace()
        }

        return null
    }

}
