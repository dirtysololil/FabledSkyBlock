package com.songoda.skyblock.limit.impl;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.island.Island;
import com.songoda.skyblock.island.IslandManager;
import com.songoda.skyblock.limit.EnumLimitation;
import com.songoda.skyblock.utils.player.PlayerUtil;
import com.songoda.skyblock.utils.version.CompatibleSpawners;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.Set;


public final class BlockLimitation extends EnumLimitation<CompatibleMaterial> {

    public BlockLimitation() {
        super(CompatibleMaterial.class);
    }

    @Override
    public String getSectionName() {
        return "block";
    }

    @Override
    public boolean hasTooMuch(long currentAmount, Enum<CompatibleMaterial> type) {
        throw new UnsupportedOperationException("Not implemented. Use getBlockLimit and isBlockLimitExceeded instead.");
    }

    @Override
    public void reload(ConfigurationSection loadFrom) {
        unload();

        if (loadFrom == null) return;

        final Set<String> keys = loadFrom.getKeys(false);

        removeAndLoadDefaultLimit(loadFrom, keys);

        for (String key : keys) {
            final String enumName = key.toUpperCase(Locale.ENGLISH);
            final CompatibleMaterial type = CompatibleMaterial.getMaterial(enumName);

            if (type == null)
                throw new IllegalArgumentException("Unable to parse Materials from '" + enumName + "' in the Section '" + loadFrom.getCurrentPath() + "'");

            getMap().put(type, loadFrom.getLong(key));
        }

    }

    @SuppressWarnings("deprecation")
    public long getBlockLimit(Player player, Block block) {
        if (player == null || block == null) return -1;

        if (player.hasPermission("fabledskyblock.limit.block.*")) return -1;

        final CompatibleMaterial material = CompatibleMaterial.getMaterial(block.getType());

        if (material == null) return -1;

        final String name = material.name().toLowerCase();

        return Math.max(getMap().getOrDefault(material, getDefault()), PlayerUtil.getNumberFromPermission(player, "fabledskyblock.limit.block." + name, true, -1));
    }

    @SuppressWarnings("deprecation")
    public boolean isBlockLimitExceeded(Block block, long limit) {

        if (limit == -1) return false;

        final IslandManager islandManager = SkyBlock.getInstance().getIslandManager();
        final Island island = islandManager.getIslandAtLocation(block.getLocation());
        final long totalPlaced;

        if (block.getType() == CompatibleMaterial.SPAWNER.getBlockMaterial()) {
            totalPlaced = island.getLevel().getMaterials().entrySet().stream().filter(x -> x.getKey().contains("SPAWNER")).mapToLong(Map.Entry::getValue).sum();
        } else {
            totalPlaced = island.getLevel().getMaterialAmount(CompatibleMaterial.getMaterial(block.getType()).name());
        }

        return limit <= totalPlaced;
    }

}
