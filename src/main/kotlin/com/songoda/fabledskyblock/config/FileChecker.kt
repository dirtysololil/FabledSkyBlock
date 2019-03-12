//package com.songoda.fabledskyblock.config
//
//import com.songoda.fabledskyblock.FabledSkyBlock
//import java.util.HashMap
//import org.bukkit.configuration.file.FileConfiguration
//import org.bukkit.configuration.file.YamlConfiguration
//import java.io.IOException
//import java.io.InputStreamReader
//import java.util.EnumMap
//
//
//class FileChecker(
//    skyblock: FabledSkyBlock, private val dataManager: DataManager, configurationFileName: String,
//    applyComments: Boolean
//) {
//
//    private val loadedResourceFiles: MutableList<FileData>
//    private val loadedCreatedFiles: MutableList<FileData>
//
//    init {
//
//        loadedResourceFiles = ArrayList()
//        loadedCreatedFiles = ArrayList()
//
//        val configFile = java.io.File(skyblock.dataFolder, configurationFileName)
//        loadedCreatedFiles.add(FileData(dataManager, configFile, YamlConfiguration.loadConfiguration(configFile)))
//
//        if (applyComments) {
//            loadedResourceFiles.add(FileData(
//                null, null, YamlConfiguration.loadConfiguration(
//                    InputStreamReader(
//                        dataManager
//                            .getConfigContent(InputStreamReader(skyblock.getResource(configurationFileName)))
//                    )
//                ))
//            )
//        } else {
//            loadedFiles[FileData.Type.RESOURCE] = FileData(
//                null, null, YamlConfiguration
//                    .loadConfiguration(InputStreamReader(skyblock.getResource(configurationFileName)))
//            )
//        }
//    }
//
//    fun loadSections() {
//        for (fileType in FileData.Type.values()) {
//            val file = loadedFiles[fileType]
//            val configLoad = file?.fileConfiguration
//
//            val configKeys = configLoad!!.getKeys(true)
//
//            for (configKeysList in configKeys) {
//                file.addKey(configKeysList, configLoad.get(configKeysList))
//            }
//        }
//    }
//
//    fun compareFiles() {
//        for (fileType in FileData.Type.values()) {
//            val file = loadedFiles[fileType]
//            val configLoad = file?.fileConfiguration
//
//            if (fileType == FileData.Type.CREATED) {
//                val resourceFile = loadedFiles[FileData.Type.RESOURCE]
//
//                for (configKeyList in file?.keys!!.keys) {
//                    if (!resourceFile?.keys!!.containsKey(configKeyList)) {
//                        configLoad!!.set(configKeyList, null)
//                    }
//                }
//            } else if (fileType == FileData.Type.RESOURCE) {
//                val createdFile = loadedFiles[FileData.Type.CREATED]
//                val createdConfigLoad = createdFile?.fileConfiguration
//
//                for (configKeyList in file?.keys!!.keys) {
//                    if (createdConfigLoad!!.getString(configKeyList) == null) {
//                        createdConfigLoad.set(configKeyList, file.keys[configKeyList])
//                    }
//                }
//            }
//        }
//    }
//
//    fun saveChanges() {
//        val file = loadedFiles[FileData.Type.CREATED]
//
//        try {
//            if (file?.file?.name == "config.yml") {
//                dataManager.saveConfig(file.fileConfiguration!!.saveToString(), file.file)
//            } else {
//                file?.fileConfiguration!!.save(file.file)
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//    }
//
//    private class FileData(dataManager: DataManager, val file: java.io.File, configLoad: FileConfiguration) {
//        var fileConfiguration: FileConfiguration? = null
//            private set
//
//        val keys: HashMap<String, Any>
//
//        init {
//            this.fileConfiguration = configLoad
//            keys = HashMap()
//
//            if (file.name == "config.yml") {
//                this.fileConfiguration = YamlConfiguration
//                    .loadConfiguration(InputStreamReader(dataManager.getConfigContent(file)))
//            }
//        }
//
//        fun addKey(key: String, obj: Any) {
//            keys[key] = obj
//        }
//
//        enum class Type {
//            CREATED, RESOURCE
//        }
//    }
//}
