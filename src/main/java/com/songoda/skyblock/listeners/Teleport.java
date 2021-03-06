package com.songoda.skyblock.listeners;

import com.songoda.core.compatibility.CompatibleSound;
import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.api.event.player.PlayerIslandEnterEvent;
import com.songoda.skyblock.api.event.player.PlayerIslandExitEvent;
import com.songoda.skyblock.api.event.player.PlayerIslandSwitchEvent;
import com.songoda.skyblock.api.island.Island;
import com.songoda.skyblock.config.FileManager;
import com.songoda.skyblock.config.FileManager.Config;
import com.songoda.skyblock.island.IslandManager;
import com.songoda.skyblock.island.IslandWorld;
import com.songoda.skyblock.message.MessageManager;
import com.songoda.skyblock.playerdata.PlayerData;
import com.songoda.skyblock.playerdata.PlayerDataManager;
import com.songoda.skyblock.sound.SoundManager;
import com.songoda.skyblock.utils.version.NMSUtil;
import com.songoda.skyblock.visit.Visit;
import com.songoda.skyblock.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.io.File;
import java.util.UUID;

public class Teleport implements Listener {

    private final SkyBlock skyblock;

    public Teleport(SkyBlock skyblock) {
        this.skyblock = skyblock;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        PlayerDataManager playerDataManager = skyblock.getPlayerDataManager();
        MessageManager messageManager = skyblock.getMessageManager();
        IslandManager islandManager = skyblock.getIslandManager();
        SoundManager soundManager = skyblock.getSoundManager();
        WorldManager worldManager = skyblock.getWorldManager();
        FileManager fileManager = skyblock.getFileManager();

        Config config = fileManager.getConfig(new File(skyblock.getDataFolder(), "language.yml"));
        FileConfiguration configLoad = config.getFileConfiguration();

        Bukkit.getScheduler().runTaskLater(skyblock, () -> islandManager.updateFlight(player), 1L);
        islandManager.loadPlayer(player);

        if (worldManager.isIslandWorld(player.getWorld())) {
            boolean isCause = false;

            if (event.getCause() == TeleportCause.ENDER_PEARL || event.getCause() == TeleportCause.NETHER_PORTAL || event.getCause() == TeleportCause.END_PORTAL) {
                isCause = true;
            } else {
                if (NMSUtil.getVersionNumber() > 9) {
                    if (event.getCause() == TeleportCause.END_GATEWAY) {
                        isCause = true;
                    }
                }
            }

            if (isCause && !islandManager.hasPermission(player, "Portal")) {
                event.setCancelled(true);

                messageManager.sendMessage(player, configLoad.getString("Island.Settings.Permission.Message"));
                soundManager.playSound(player,  CompatibleSound.ENTITY_VILLAGER_NO.getSound(), 1.0F, 1.0F);

                return;
            }

            if (isCause && event.getCause() != TeleportCause.ENDER_PEARL) {
                event.setCancelled(true);
                return;
            }
        }

        if (playerDataManager.hasPlayerData(player)) {
            PlayerData playerData = playerDataManager.getPlayerData(player);

            com.songoda.skyblock.island.Island island = islandManager.getIslandAtLocation(event.getTo());

            if (island != null) {
                if (!island.getOwnerUUID().equals(playerData.getOwner())) {
                    if (!player.hasPermission("fabledskyblock.bypass") && !player.hasPermission("fabledskyblock.bypass.*") && !player.hasPermission("fabledskyblock.*")) {
                        if (!island.isOpen() && !island.isCoopPlayer(player.getUniqueId())) {
                            event.setCancelled(true);

                            messageManager.sendMessage(player, configLoad.getString("Island.Visit.Closed.Plugin.Message"));
                            soundManager.playSound(player, CompatibleSound.BLOCK_ANVIL_LAND.getSound(), 1.0F, 1.0F);

                            return;
                        } else if (fileManager.getConfig(new File(skyblock.getDataFolder(), "config.yml")).getFileConfiguration().getBoolean("Island.Visitor.Banning") && island.getBan().isBanned(player.getUniqueId())) {
                            event.setCancelled(true);

                            messageManager.sendMessage(player, configLoad.getString("Island.Visit.Banned.Teleport.Message"));
                            soundManager.playSound(player, CompatibleSound.BLOCK_ANVIL_LAND.getSound(), 1.0F, 1.0F);

                            return;
                        }
                    }
                }

                if (playerData.getIsland() != null && !playerData.getIsland().equals(island.getOwnerUUID())) {
                    Island exitIsland = null;

                    OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(playerData.getIsland());

                    if (islandManager.containsIsland(playerData.getIsland())) {
                        exitIsland = islandManager.getIsland(offlinePlayer).getAPIWrapper();
                    }

                    Bukkit.getServer().getPluginManager().callEvent(new PlayerIslandExitEvent(player, exitIsland));
                    Bukkit.getServer().getPluginManager().callEvent(new PlayerIslandSwitchEvent(player, exitIsland, island.getAPIWrapper()));

                    playerData.setVisitTime(0);
                }

                if (worldManager.getIslandWorld(event.getTo().getWorld()) == IslandWorld.Normal) {
                    if (!island.isWeatherSynchronized()) {
                        player.setPlayerTime(island.getTime(), fileManager.getConfig(new File(skyblock.getDataFolder(), "config.yml")).getFileConfiguration().getBoolean("Island.Weather.Time.Cycle"));
                        player.setPlayerWeather(island.getWeather());
                    }
                }

                UUID islandOwnerUUID = playerData.getIsland();
                playerData.setIsland(island.getOwnerUUID());

                if (islandOwnerUUID != null && islandManager.containsIsland(islandOwnerUUID) && (playerData.getOwner() == null || !playerData.getOwner().equals(islandOwnerUUID))) {
                    islandManager.unloadIsland(islandManager.getIsland(Bukkit.getServer().getOfflinePlayer(islandOwnerUUID)), null);
                }

                Visit visit = island.getVisit();

                if (visit != null && !visit.isVisitor(player.getUniqueId())) {
                    Bukkit.getServer().getPluginManager().callEvent(new PlayerIslandEnterEvent(player, island.getAPIWrapper()));

                    visit.addVisitor(player.getUniqueId());
                    visit.save();
                }

                return;
            }

            player.resetPlayerTime();
            player.resetPlayerWeather();

            if (playerData.getIsland() != null) {
                Island islandWrapper = null;
                island = islandManager.getIsland(Bukkit.getServer().getOfflinePlayer(playerData.getIsland()));

                if (island != null) {
                    islandWrapper = island.getAPIWrapper();
                }

                Bukkit.getServer().getPluginManager().callEvent(new PlayerIslandExitEvent(player, islandWrapper));

                playerData.setVisitTime(0);
            }

            UUID islandOwnerUUID = playerData.getIsland();
            playerData.setIsland(null);

            if (islandOwnerUUID != null && islandManager.containsIsland(islandOwnerUUID) && (playerData.getOwner() == null || !playerData.getOwner().equals(islandOwnerUUID))) {
                islandManager.unloadIsland(islandManager.getIsland(Bukkit.getServer().getOfflinePlayer(islandOwnerUUID)), null);
            }
        }
    }
    
    @EventHandler
	public void onEntityTeleport(EntityPortalEvent e) {
		WorldManager worldManager = skyblock.getWorldManager();
		// Do not handle player
		if (e.getEntityType() == EntityType.PLAYER)
			return;
		Location from = e.getFrom();
		Location to = e.getTo();
		// Test which world the event is from
		if (from.getWorld() == to.getWorld())
			return;

		if (worldManager.getIslandWorld(e.getFrom().getWorld()) != null)
			e.setCancelled(true);
	}
}
