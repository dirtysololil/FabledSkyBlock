package com.songoda.skyblock.menus;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.CompatibleSound;
import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.config.FileManager;
import com.songoda.skyblock.island.Island;
import com.songoda.skyblock.island.IslandManager;
import com.songoda.skyblock.island.IslandRole;
import com.songoda.skyblock.placeholder.Placeholder;
import com.songoda.skyblock.playerdata.PlayerData;
import com.songoda.skyblock.playerdata.PlayerDataManager;
import com.songoda.skyblock.sound.SoundManager;
import com.songoda.skyblock.utils.NumberUtil;
import com.songoda.skyblock.utils.item.SkullUtil;
import com.songoda.skyblock.utils.item.nInventoryUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class Visitors {

    private static Visitors instance;

    public static Visitors getInstance() {
        if (instance == null) {
            instance = new Visitors();
        }

        return instance;
    }

    public void open(Player player) {
        SkyBlock skyblock = SkyBlock.getInstance();

        PlayerDataManager playerDataManager = skyblock.getPlayerDataManager();
        IslandManager islandManager = skyblock.getIslandManager();
        SoundManager soundManager = skyblock.getSoundManager();
        FileManager fileManager = skyblock.getFileManager();

        if (playerDataManager.hasPlayerData(player)) {
            FileConfiguration configLoad = fileManager.getConfig(new File(skyblock.getDataFolder(), "language.yml"))
                    .getFileConfiguration();

            nInventoryUtil nInv = new nInventoryUtil(player, event -> {
                if (playerDataManager.hasPlayerData(player)) {
                    PlayerData playerData = skyblock.getPlayerDataManager().getPlayerData(player);
                    Island island = islandManager.getIsland(player);

                    if (island == null) {
                        skyblock.getMessageManager().sendMessage(player,
                                configLoad.getString("Command.Island.Visitors.Owner.Message"));
                        soundManager.playSound(player, CompatibleSound.BLOCK_ANVIL_LAND.getSound(), 1.0F, 1.0F);

                        return;
                    }

                    ItemStack is = event.getItem();

                    if ((is.getType() == CompatibleMaterial.BLACK_STAINED_GLASS_PANE.getMaterial()) && (is.hasItemMeta())
                            && (is.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&',
                            configLoad.getString("Menu.Visitors.Item.Barrier.Displayname"))))) {
                        soundManager.playSound(player, CompatibleSound.BLOCK_GLASS_BREAK.getSound(), 1.0F, 1.0F);

                        event.setWillClose(false);
                        event.setWillDestroy(false);
                    } else if ((is.getType() == CompatibleMaterial.OAK_FENCE_GATE.getMaterial()) && (is.hasItemMeta())
                            && (is.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&',
                            configLoad.getString("Menu.Visitors.Item.Exit.Displayname"))))) {
                        soundManager.playSound(player, CompatibleSound.BLOCK_CHEST_CLOSE.getSound(), 1.0F, 1.0F);
                    } else if ((is.getType() == Material.PAINTING) && (is.hasItemMeta())
                            && (is.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&',
                            configLoad.getString("Menu.Visitors.Item.Statistics.Displayname"))))) {
                        soundManager.playSound(player, CompatibleSound.ENTITY_VILLAGER_YES.getSound(), 1.0F, 1.0F);

                        event.setWillClose(false);
                        event.setWillDestroy(false);
                    } else if ((is.getType() == Material.BARRIER) && (is.hasItemMeta())
                            && (is.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&',
                            configLoad.getString("Menu.Visitors.Item.Nothing.Displayname"))))) {
                        soundManager.playSound(player, CompatibleSound.BLOCK_ANVIL_LAND.getSound(), 1.0F, 1.0F);

                        event.setWillClose(false);
                        event.setWillDestroy(false);
                    } else if ((is.getType() == SkullUtil.createItemStack().getType()) && (is.hasItemMeta())) {
                        if (is.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&',
                                configLoad.getString("Menu.Visitors.Item.Previous.Displayname")))) {
                            playerData.setPage(playerData.getPage() - 1);
                            soundManager.playSound(player, CompatibleSound.ENTITY_ARROW_HIT.getSound(), 1.0F, 1.0F);

                            Bukkit.getServer().getScheduler().runTaskLater(skyblock, () -> open(player), 1L);
                        } else if (is.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes(
                                '&', configLoad.getString("Menu.Visitors.Item.Next.Displayname")))) {
                            playerData.setPage(playerData.getPage() + 1);
                            soundManager.playSound(player, CompatibleSound.ENTITY_ARROW_HIT.getSound(), 1.0F, 1.0F);

                            Bukkit.getServer().getScheduler().runTaskLater(skyblock, () -> open(player), 1L);
                        } else {
                            boolean isOperator = island.hasRole(IslandRole.Operator, player.getUniqueId()),
                                    isOwner = island.hasRole(IslandRole.Owner, player.getUniqueId()),
                                    canKick = island.getSetting(IslandRole.Operator, "Kick").getStatus(),
                                    canBan = island.getSetting(IslandRole.Operator, "Ban").getStatus(),
                                    banningEnabled = fileManager
                                            .getConfig(new File(skyblock.getDataFolder(), "config.yml"))
                                            .getFileConfiguration().getBoolean("Island.Visitor.Banning");
                            String playerName = ChatColor.stripColor(is.getItemMeta().getDisplayName());

                            if ((isOperator && canKick) || isOwner) {
                                if (banningEnabled && ((isOperator && canBan) || isOwner)) {
                                    if (event.getClick() == ClickType.LEFT) {
                                        Bukkit.getServer().dispatchCommand(player, "island kick " + playerName);
                                    } else if (event.getClick() == ClickType.RIGHT) {
                                        Bukkit.getServer().dispatchCommand(player, "island ban " + playerName);
                                    } else {
                                        soundManager.playSound(player, CompatibleSound.ENTITY_CHICKEN_EGG.getSound(), 1.0F,
                                                1.0F);

                                        event.setWillClose(false);
                                        event.setWillDestroy(false);

                                        return;
                                    }
                                } else {
                                    Bukkit.getServer().dispatchCommand(player, "island kick " + playerName);
                                }
                            } else {
                                if (banningEnabled && ((isOperator && canBan) || isOwner)) {
                                    Bukkit.getServer().dispatchCommand(player, "island ban " + playerName);
                                } else {
                                    soundManager.playSound(player, CompatibleSound.ENTITY_CHICKEN_EGG.getSound(), 1.0F,
                                            1.0F);

                                    event.setWillClose(false);
                                    event.setWillDestroy(false);

                                    return;
                                }
                            }

                            Bukkit.getServer().getScheduler().runTaskLater(skyblock, () -> open(player), 1L);
                        }
                    }
                }
            });

            PlayerData playerData = playerDataManager.getPlayerData(player);
            Island island = skyblock.getIslandManager().getIsland(player);

            Set<UUID> islandVisitors = islandManager.getVisitorsAtIsland(island);
            Map<Integer, UUID> sortedIslandVisitors = new TreeMap<>();

            nInv.addItem(nInv.createItem(CompatibleMaterial.OAK_FENCE_GATE.getItem(),
                    configLoad.getString("Menu.Visitors.Item.Exit.Displayname"), null, null, null, null), 0, 8);
            nInv.addItem(
                    nInv.createItem(new ItemStack(Material.PAINTING),
                            configLoad.getString("Menu.Visitors.Item.Statistics.Displayname"),
                            configLoad.getStringList("Menu.Visitors.Item.Statistics.Lore"),
                            new Placeholder[]{new Placeholder("%visitors", "" + islandVisitors.size())}, null, null),
                    4);
            nInv.addItem(
                    nInv.createItem(CompatibleMaterial.BLACK_STAINED_GLASS_PANE.getItem(),
                            configLoad.getString("Menu.Visitors.Item.Barrier.Displayname"), null, null, null, null),
                    9, 10, 11, 12, 13, 14, 15, 16, 17);

            for (UUID islandVisitorList : islandVisitors) {
                sortedIslandVisitors.put(
                        playerDataManager.getPlayerData(Bukkit.getServer().getPlayer(islandVisitorList)).getVisitTime(),
                        islandVisitorList);
            }

            islandVisitors.clear();

            for (int sortedIslandVisitorList : sortedIslandVisitors.keySet()) {
                islandVisitors.add(sortedIslandVisitors.get(sortedIslandVisitorList));
            }

            int playerMenuPage = playerData.getPage(), nextEndIndex = sortedIslandVisitors.size() - playerMenuPage * 36;

            if (playerMenuPage != 1) {
                nInv.addItem(nInv.createItem(SkullUtil.create(
                        "ToR1w9ZV7zpzCiLBhoaJH3uixs5mAlMhNz42oaRRvrG4HRua5hC6oyyOPfn2HKdSseYA9b1be14fjNRQbSJRvXF3mlvt5/zct4sm+cPVmX8K5kbM2vfwHJgCnfjtPkzT8sqqg6YFdT35mAZGqb9/xY/wDSNSu/S3k2WgmHrJKirszaBZrZfnVnqITUOgM9TmixhcJn2obeqICv6tl7/Wyk/1W62wXlXGm9+WjS+8rRNB+vYxqKR3XmH2lhAiyVGbADsjjGtBVUTWjq+aPw670SjXkoii0YE8sqzUlMMGEkXdXl9fvGtnWKk3APSseuTsjedr7yq+AkXFVDqqkqcUuXwmZl2EjC2WRRbhmYdbtY5nEfqh5+MiBrGdR/JqdEUL4yRutyRTw8mSUAI6X2oSVge7EdM/8f4HwLf33EO4pTocTqAkNbpt6Z54asLe5Y12jSXbvd2dFsgeJbrslK7e4uy/TK8CXf0BP3KLU20QELYrjz9I70gtj9lJ9xwjdx4/xJtxDtrxfC4Afmpu+GNYA/mifpyP3GDeBB5CqN7btIvEWyVvRNH7ppAqZIPqYJ7dSDd2RFuhAId5Yq98GUTBn+eRzeigBvSi1bFkkEgldfghOoK5WhsQtQbXuBBXITMME3NaWCN6zG7DxspS6ew/rZ8E809Xe0ArllquIZ0sP+k=",
                        "eyJ0aW1lc3RhbXAiOjE0OTU3NTE5MTYwNjksInByb2ZpbGVJZCI6ImE2OGYwYjY0OGQxNDQwMDBhOTVmNGI5YmExNGY4ZGY5IiwicHJvZmlsZU5hbWUiOiJNSEZfQXJyb3dMZWZ0Iiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zZWJmOTA3NDk0YTkzNWU5NTViZmNhZGFiODFiZWFmYjkwZmI5YmU0OWM3MDI2YmE5N2Q3OThkNWYxYTIzIn19fQ=="),
                        configLoad.getString("Menu.Visitors.Item.Previous.Displayname"), null, null, null, null), 1);
            }

            if (!(nextEndIndex == 0 || nextEndIndex < 0)) {
                nInv.addItem(nInv.createItem(SkullUtil.create(
                        "wZPrsmxckJn4/ybw/iXoMWgAe+1titw3hjhmf7bfg9vtOl0f/J6YLNMOI0OTvqeRKzSQVCxqNOij6k2iM32ZRInCQyblDIFmFadQxryEJDJJPVs7rXR6LRXlN8ON2VDGtboRTL7LwMGpzsrdPNt0oYDJLpR0huEeZKc1+g4W13Y4YM5FUgEs8HvMcg4aaGokSbvrYRRcEh3LR1lVmgxtbiUIr2gZkR3jnwdmZaIw/Ujw28+Et2pDMVCf96E5vC0aNY0KHTdMYheT6hwgw0VAZS2VnJg+Gz4JCl4eQmN2fs4dUBELIW2Rdnp4U1Eb+ZL8DvTV7ofBeZupknqPOyoKIjpInDml9BB2/EkD3zxFtW6AWocRphn03Z203navBkR6ztCMz0BgbmQU/m8VL/s8o4cxOn+2ppjrlj0p8AQxEsBdHozrBi8kNOGf1j97SDHxnvVAF3X8XDso+MthRx5pbEqpxmLyKKgFh25pJE7UaMSnzH2lc7aAZiax67MFw55pDtgfpl+Nlum4r7CK2w5Xob2QTCovVhu78/6SV7qM2Lhlwx/Sjqcl8rn5UIoyM49QE5Iyf1tk+xHXkIvY0m7q358oXsfca4eKmxMe6DFRjUDo1VuWxdg9iVjn22flqz1LD1FhGlPoqv0k4jX5Q733LwtPPI6VOTK+QzqrmiuR6e8=",
                        "eyJ0aW1lc3RhbXAiOjE0OTM4NjgxMDA2NzMsInByb2ZpbGVJZCI6IjUwYzg1MTBiNWVhMDRkNjBiZTlhN2Q1NDJkNmNkMTU2IiwicHJvZmlsZU5hbWUiOiJNSEZfQXJyb3dSaWdodCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI2ZjFhMjViNmJjMTk5OTQ2NDcyYWVkYjM3MDUyMjU4NGZmNmY0ZTgzMjIxZTU5NDZiZDJlNDFiNWNhMTNiIn19fQ=="),
                        configLoad.getString("Menu.Visitors.Item.Next.Displayname"), null, null, null, null), 7);
            }

            if (islandVisitors.size() == 0) {
                nInv.addItem(
                        nInv.createItem(new ItemStack(Material.BARRIER),
                                configLoad.getString("Menu.Visitors.Item.Nothing.Displayname"), null, null, null, null),
                        31);
            } else {
                boolean isOperator = island.hasRole(IslandRole.Operator, player.getUniqueId()),
                        isOwner = island.hasRole(IslandRole.Owner, player.getUniqueId()),
                        canKick = island.getSetting(IslandRole.Operator, "Kick").getStatus(),
                        canBan = island.getSetting(IslandRole.Operator, "Ban").getStatus(),
                        banningEnabled = fileManager.getConfig(new File(skyblock.getDataFolder(), "config.yml"))
                                .getFileConfiguration().getBoolean("Island.Visitor.Banning");
                int index = playerMenuPage * 36 - 36,
                        endIndex = index >= islandVisitors.size() ? islandVisitors.size() - 1 : index + 36,
                        inventorySlot = 17;

                for (; index < endIndex; index++) {
                    if (islandVisitors.size() > index) {
                        inventorySlot++;

                        Player targetPlayer = Bukkit.getServer().getPlayer((UUID) islandVisitors.toArray()[index]);
                        PlayerData targetPlayerData = playerDataManager.getPlayerData(targetPlayer);

                        String[] targetPlayerTexture = targetPlayerData.getTexture();
                        String islandVisitTimeFormatted;

                        long[] islandVisitTime = NumberUtil.getDuration(targetPlayerData.getVisitTime());

                        if (islandVisitTime[0] != 0) {
                            islandVisitTimeFormatted = islandVisitTime[0] + " "
                                    + configLoad.getString("Menu.Visitors.Item.Visitor.Word.Days") + ", "
                                    + islandVisitTime[1] + " "
                                    + configLoad.getString("Menu.Visitors.Item.Visitor.Word.Hours") + ", "
                                    + islandVisitTime[2] + " "
                                    + configLoad.getString("Menu.Visitors.Item.Visitor.Word.Minutes") + ", "
                                    + islandVisitTime[3] + " "
                                    + configLoad.getString("Menu.Visitors.Item.Visitor.Word.Seconds");
                        } else if (islandVisitTime[1] != 0) {
                            islandVisitTimeFormatted = islandVisitTime[1] + " "
                                    + configLoad.getString("Menu.Visitors.Item.Visitor.Word.Hours") + ", "
                                    + islandVisitTime[2] + " "
                                    + configLoad.getString("Menu.Visitors.Item.Visitor.Word.Minutes") + ", "
                                    + islandVisitTime[3] + " "
                                    + configLoad.getString("Menu.Visitors.Item.Visitor.Word.Seconds");
                        } else if (islandVisitTime[2] != 0) {
                            islandVisitTimeFormatted = islandVisitTime[2] + " "
                                    + configLoad.getString("Menu.Visitors.Item.Visitor.Word.Minutes") + ", "
                                    + islandVisitTime[3] + " "
                                    + configLoad.getString("Menu.Visitors.Item.Visitor.Word.Seconds");
                        } else {
                            islandVisitTimeFormatted = islandVisitTime[3] + " "
                                    + configLoad.getString("Menu.Visitors.Item.Visitor.Word.Seconds");
                        }

                        List<String> itemLore = new ArrayList<>();

                        if ((isOperator && canKick) || isOwner) {
                            if (banningEnabled && ((isOperator && canBan) || isOwner)) {
                                itemLore.addAll(configLoad.getStringList(
                                        "Menu.Visitors.Item.Visitor.Kick.Permission.Ban.Permission.Lore"));
                            } else {
                                itemLore.addAll(configLoad.getStringList(
                                        "Menu.Visitors.Item.Visitor.Kick.Permission.Ban.NoPermission.Lore"));
                            }
                        } else {
                            if (banningEnabled && ((isOperator && canBan) || isOwner)) {
                                itemLore.addAll(configLoad.getStringList(
                                        "Menu.Visitors.Item.Visitor.Kick.NoPermission.Ban.Permission.Lore"));
                            } else {
                                itemLore.addAll(configLoad.getStringList(
                                        "Menu.Visitors.Item.Visitor.Kick.NoPermission.Ban.NoPermission.Lore"));
                            }
                        }

                        nInv.addItem(
                                nInv.createItem(SkullUtil.create(targetPlayerTexture[0], targetPlayerTexture[1]),
                                        ChatColor.translateAlternateColorCodes('&',
                                                configLoad.getString("Menu.Visitors.Item.Visitor.Displayname")
                                                        .replace("%player", targetPlayer.getName())),
                                        itemLore,
                                        new Placeholder[]{new Placeholder("%time", islandVisitTimeFormatted)}, null,
                                        null),
                                inventorySlot);
                    }
                }
            }

            nInv.setTitle(ChatColor.translateAlternateColorCodes('&', configLoad.getString("Menu.Visitors.Title")));
            nInv.setRows(6);

            Bukkit.getServer().getScheduler().runTask(skyblock, () -> nInv.open());
        }
    }
}
