package com.songoda.skyblock.levelling.rework.calculator.impl;

import org.bukkit.block.CreatureSpawner;

import com.songoda.skyblock.levelling.rework.calculator.SpawnerCalculator;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.spawner.SpawnerStack;

public class UltimateStackerCalculator implements SpawnerCalculator {

    @Override
    public long getSpawnerAmount(CreatureSpawner spawner) {
        if (!UltimateStacker.getInstance().getConfig().getBoolean("Spawners.Enabled")) return 0;

        final SpawnerStack stack = UltimateStacker.getInstance().getSpawnerStackManager().getSpawner(spawner.getLocation());
        return stack == null ? 0 : stack.getAmount();
    }

}
