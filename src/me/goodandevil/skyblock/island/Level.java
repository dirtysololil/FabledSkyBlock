package me.goodandevil.skyblock.island;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import me.goodandevil.skyblock.Main;
import me.goodandevil.skyblock.config.FileManager.Config;

public class Level {
	
	private final Main plugin;
	private final Island island;
	
	private int lastLevel = 0;
	private int lastPoints = 0;
	
	private Map<String, Integer> materials;
	
	public Level(Island island, Main plugin) {
		this.island = island;
		this.plugin = plugin;
		
		Config config = plugin.getFileManager().getConfig(new File(new File(plugin.getDataFolder().toString() + "/island-data"), island.getOwnerUUID().toString() + ".yml"));
		FileConfiguration configLoad = config.getFileConfiguration();
		
		Map<String, Integer> materials = new HashMap<>();
		
		if (configLoad.getString("Levelling.Materials") != null) {
			for (String materialList : configLoad.getConfigurationSection("Levelling.Materials").getKeys(false)) {
				if (configLoad.getString("Levelling.Materials." + materialList + ".Amount") != null) {
					materials.put(materialList, configLoad.getInt("Levelling.Materials." + materialList + ".Amount"));					
				}
			}
		}
		
		this.materials = materials;
	}
	
	public int getPoints() {
		Config config = plugin.getFileManager().getConfig(new File(plugin.getDataFolder(), "levelling.yml"));
		FileConfiguration configLoad = config.getFileConfiguration();
		
		int pointsEarned = 0;
		
		for (String materialList : materials.keySet()) {
			int materialAmount = materials.get(materialList);
			
			if (configLoad.getString("Materials." + materialList + ".Points") != null) {
				int pointsRequired = config.getFileConfiguration().getInt("Materials." + materialList + ".Points");
				
				if (pointsRequired != 0) {
					pointsEarned = pointsEarned + (materialAmount*pointsRequired);
				}	
			}
		}
		
		return pointsEarned;
	}
	
	public int getLevel() {
		int division = plugin.getFileManager().getConfig(new File(plugin.getDataFolder(), "config.yml")).getFileConfiguration().getInt("Island.Levelling.Division");
		
		if (division == 0) {
			division = 1;
		}
		
		return getPoints() / division;
	}
	
	public void setMaterials(Map<String, Integer> materials) {
		Config config = plugin.getFileManager().getConfig(new File(new File(plugin.getDataFolder().toString() + "/island-data"), island.getOwnerUUID().toString() + ".yml"));
		File configFile = config.getFile();
		FileConfiguration configLoad = config.getFileConfiguration();
		
		configLoad.set("Levelling.Materials", null);
		
		for (String materialList : materials.keySet()) {
			configLoad.set("Levelling.Materials." + materialList + ".Amount", materials.get(materialList));
		}
		
		try {
			configLoad.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.materials = materials;
	}
	
	public Map<String, Integer> getMaterials() {
		return materials;
	}
	
	public void setLastPoints(int lastPoints) {
		this.lastPoints = lastPoints;
	}
	
	public int getLastPoints() {
		return lastPoints;
	}
	
	public void setLastLevel(int lastLevel) {
		this.lastLevel = lastLevel;
	}
	
	public int getLastLevel() {
		return lastLevel;
	}
}