package com.songoda.skyblock.api.levelling;

import com.google.common.base.Preconditions;
import com.songoda.skyblock.api.island.Island;

public class LevellingManager {

    private final com.songoda.skyblock.levelling.rework.IslandLevelManager levellingManager;

    public LevellingManager(com.songoda.skyblock.levelling.rework.IslandLevelManager levellingManager) {
        this.levellingManager = levellingManager;
    }

    /**
     * Calculates the points of an Island to determine what the Island level is
     */
    public void calculatePoints(Island island) {
        Preconditions.checkArgument(island != null, "Cannot calculate points to null island");
        this.levellingManager.startScan(null, island.getIsland());
    }
}
