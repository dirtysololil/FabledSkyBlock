package com.songoda.skyblock.listeners;

import com.songoda.core.compatibility.CompatibleSound;
import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.config.FileManager;
import com.songoda.skyblock.config.FileManager.Config;
import com.songoda.skyblock.island.*;
import com.songoda.skyblock.message.MessageManager;
import com.songoda.skyblock.playerdata.PlayerData;
import com.songoda.skyblock.playerdata.PlayerDataManager;
import com.songoda.skyblock.sound.SoundManager;
import com.songoda.skyblock.utils.version.NMSUtil;
import com.songoda.skyblock.utils.world.LocationUtil;
import com.songoda.skyblock.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;

import java.io.File;

public class Move implements Listener {

    private final SkyBlock skyblock;

    public Move(SkyBlock skyblock) {
        this.skyblock = skyblock;
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())) {
            return;
        }

        PlayerDataManager playerDataManager = skyblock.getPlayerDataManager();
        MessageManager messageManager = skyblock.getMessageManager();
        IslandManager islandManager = skyblock.getIslandManager();
        SoundManager soundManager = skyblock.getSoundManager();
        WorldManager worldManager = skyblock.getWorldManager();
        FileManager fileManager = skyblock.getFileManager();

        if (!worldManager.isIslandWorld(player.getWorld())) return;

        IslandWorld world = worldManager.getIslandWorld(player.getWorld());

        if (world == IslandWorld.Nether || world == IslandWorld.End) {
            if (!fileManager.getConfig(new File(skyblock.getDataFolder(), "config.yml")).getFileConfiguration().getBoolean("Island.World." + world.name() + ".Enable")) {
                Config config = fileManager.getConfig(new File(skyblock.getDataFolder(), "language.yml"));
                FileConfiguration configLoad = config.getFileConfiguration();

                messageManager.sendMessage(player, configLoad.getString("Island.World.Message").replace(configLoad.getString("Island.World.Word." + world.name()), world.name()));

                if (playerDataManager.hasPlayerData(player)) {
                    PlayerData playerData = playerDataManager.getPlayerData(player);

                    if (playerData.getIsland() != null) {
                        Island island = islandManager.getIsland(Bukkit.getServer().getOfflinePlayer(playerData.getIsland()));

                        if (island != null) {
                            if (island.hasRole(IslandRole.Member, player.getUniqueId()) || island.hasRole(IslandRole.Operator, player.getUniqueId())
                                    || island.hasRole(IslandRole.Owner, player.getUniqueId())) {
                                player.teleport(island.getLocation(IslandWorld.Normal, IslandEnvironment.Main));
                            } else {
                                player.teleport(island.getLocation(IslandWorld.Normal, IslandEnvironment.Visitor));
                            }

                            player.setFallDistance(0.0F);
                            soundManager.playSound(player, CompatibleSound.ENTITY_ENDERMAN_TELEPORT.getSound(), 1.0F, 1.0F);

                            return;
                        }
                    }
                }

                LocationUtil.teleportPlayerToSpawn(player);
                soundManager.playSound(player, CompatibleSound.ENTITY_ENDERMAN_TELEPORT.getSound(), 1.0F, 1.0F);
            }
        }

        if (playerDataManager.hasPlayerData(player)) {
            PlayerData playerData = playerDataManager.getPlayerData(player);

            if (playerData.getIsland() != null) {
                Island island = islandManager.getIsland(Bukkit.getServer().getOfflinePlayer(playerData.getIsland()));

                if (island != null) {
                    if (islandManager.isLocationAtIsland(island, to)) {
                        Config config = fileManager.getConfig(new File(skyblock.getDataFolder(), "config.yml"));
                        FileConfiguration configLoad = config.getFileConfiguration();

                        boolean keepItemsOnDeath;

                        if (configLoad.getBoolean("Island.Settings.KeepItemsOnDeath.Enable")) {
                            keepItemsOnDeath = island.getSetting(IslandRole.Owner, "KeepItemsOnDeath").getStatus();
                        } else {
                            keepItemsOnDeath = configLoad.getBoolean("Island.KeepItemsOnDeath.Enable");
                        }

                        if (configLoad.getBoolean("Island.World." + world.name() + ".Liquid.Enable")) {
                            if (to.getY() <= configLoad.getInt("Island.World." + world.name() + ".Liquid.Height")) {
                                if (keepItemsOnDeath && configLoad.getBoolean("Island.Liquid.Teleport.Enable")) {
                                    player.setFallDistance(0.0F);

                                    if (island.hasRole(IslandRole.Member, player.getUniqueId()) || island.hasRole(IslandRole.Operator, player.getUniqueId())
                                            || island.hasRole(IslandRole.Owner, player.getUniqueId())) {
                                        player.teleport(island.getLocation(IslandWorld.Normal, IslandEnvironment.Main));
                                    } else {
                                        player.teleport(island.getLocation(IslandWorld.Normal, IslandEnvironment.Visitor));
                                    }

                                    player.setFallDistance(0.0F);
                                    soundManager.playSound(player, CompatibleSound.ENTITY_ENDERMAN_TELEPORT.getSound(), 1.0F, 1.0F);
                                }
                                return;
                            }
                        }

                        if (configLoad.getBoolean("Island.Void.Teleport.Enable")) {
                            if (to.getY() <= configLoad.getInt("Island.Void.Teleport.Offset")) {
                                if (configLoad.getBoolean("Island.Void.Teleport.ClearInventory")) {
                                    player.getInventory().clear();
                                    player.setLevel(0);
                                    player.setExp(0.0F);

                                    if (NMSUtil.getVersionNumber() > 8) {
                                        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                                    } else {
                                        player.setHealth(player.getMaxHealth());
                                    }

                                    player.setFoodLevel(20);

                                    for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                                        player.removePotionEffect(potionEffect.getType());
                                    }
                                }

                                player.setFallDistance(0.0F);

                                if (configLoad.getBoolean("Island.Void.Teleport.Island")) {
                                    if (island.hasRole(IslandRole.Member, player.getUniqueId()) || island.hasRole(IslandRole.Operator, player.getUniqueId())
                                            || island.hasRole(IslandRole.Owner, player.getUniqueId())) {
                                        player.teleport(island.getLocation(IslandWorld.Normal, IslandEnvironment.Main));
                                    } else {
                                        player.teleport(island.getLocation(IslandWorld.Normal, IslandEnvironment.Visitor));
                                    }
                                } else {
                                    LocationUtil.teleportPlayerToSpawn(player);
                                }

                                player.setFallDistance(0.0F);
                                soundManager.playSound(player, CompatibleSound.ENTITY_ENDERMAN_TELEPORT.getSound(), 1.0F, 1.0F);
                            }
                        }
                    } else {
                        if (!LocationUtil.isLocationAtLocationRadius(island.getLocation(world, IslandEnvironment.Island), to, island.getRadius() + 0.5)) {
                            if (island.getVisit().isVisitor(player.getUniqueId())) {
                                player.teleport(island.getLocation(world, IslandEnvironment.Visitor));
                            } else {
                                player.teleport(island.getLocation(world, IslandEnvironment.Main));
                            }

                            player.setFallDistance(0.0F);
                            messageManager.sendMessage(player, skyblock.getFileManager().getConfig(new File(skyblock.getDataFolder(), "language.yml")).getFileConfiguration()
                                    .getString("Island.WorldBorder.Outside.Message"));
                            soundManager.playSound(player, CompatibleSound.ENTITY_ENDERMAN_TELEPORT.getSound(), 1.0F, 1.0F);
                        }
                    }

                    return;
                }
            }

            // Load the island they are now on if one exists
            if (player.hasPermission("fabledskyblock.bypass")) {
                Island loadedIsland = islandManager.loadIslandAtLocation(player.getLocation());
                if (loadedIsland != null) {
                    playerData.setIsland(loadedIsland.getOwnerUUID());
                    return;
                }
            }

            LocationUtil.teleportPlayerToSpawn(player);

            messageManager.sendMessage(player,
                    skyblock.getFileManager().getConfig(new File(skyblock.getDataFolder(), "language.yml")).getFileConfiguration().getString("Island.WorldBorder.Disappeared.Message"));
            soundManager.playSound(player, CompatibleSound.ENTITY_ENDERMAN_TELEPORT.getSound(), 1.0F, 1.0F);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {

        final Player player = e.getPlayer();
        final WorldManager worldManager = skyblock.getWorldManager();

        if (e.getTo() == null ||
                !worldManager.isIslandWorld(e.getTo().getWorld()) ||
                skyblock.getIslandManager().getIslandAtLocation(e.getTo()) != null)
            return;

        e.setCancelled(true);

        skyblock.getMessageManager().sendMessage(player,
                skyblock.getFileManager().getConfig(new File(skyblock.getDataFolder(), "language.yml")).getFileConfiguration().getString("Island.WorldBorder.Disappeared.Message"));
        skyblock.getSoundManager().playSound(player, CompatibleSound.ENTITY_ENDERMAN_TELEPORT.getSound(), 1.0F, 1.0F);

    }
}
