package com.songoda.fabledskyblock.world

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import java.util.*
import javax.swing.Spring.height



class VoidGenerator : ChunkGenerator() {

    override fun generateChunkData(world: World?, random: Random?, x: Int, z: Int, biome: BiomeGrid?): ChunkData {
        val chunkData = createChunkData(world)

        // TODO: Implement

        return chunkData
    }

    override fun getDefaultPopulators(world: World?): MutableList<BlockPopulator> = mutableListOf()

    override fun canSpawn(world: World?, x: Int, z: Int): Boolean = true

    fun generateBlockSections(world: World?, random: Random, chunkX: Int, chunkZ: Int, biomeGrid: BiomeGrid): Array<ByteArray> = Array(world?.maxHeight ?: 256 / 16) { ByteArray(0) }

    private fun setBlock(chunkData: ChunkData, material: Material, height: Int) {
        for (x in 0..15) {
            for (z in 0..15) {
                for (y in 0 until height) {
                    chunkData.setBlock(x, y, z, material)
                }
            }
        }
    }

}
