package com.songoda.skyblock.utils.item;

 
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.skyblock.utils.version.NMSUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Constructor;
import java.math.BigInteger;

public final class ItemStackUtil {

    public static ItemStack deserializeItemStack(String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        ItemStack itemStack = null;

        try {
            Class<?> NBTTagCompoundClass = NMSUtil.getNMSClass("NBTTagCompound");
            Class<?> NMSItemStackClass = NMSUtil.getNMSClass("ItemStack");
            Object NBTTagCompound = NMSUtil.getNMSClass("NBTCompressedStreamTools")
                    .getMethod("a", DataInputStream.class).invoke(null, dataInputStream);
            Object craftItemStack;

            if (NMSUtil.getVersionNumber() > 12) {
                craftItemStack = NMSItemStackClass.getMethod("a", NBTTagCompoundClass).invoke(null, NBTTagCompound);
            } else if (NMSUtil.getVersionNumber() > 10) {
                craftItemStack = NMSItemStackClass.getConstructor(NBTTagCompoundClass).newInstance(NBTTagCompound);
            } else {
                craftItemStack = NMSItemStackClass.getMethod("createStack", NBTTagCompoundClass).invoke(null,
                        NBTTagCompound);
            }

            itemStack = (ItemStack) NMSUtil.getCraftClass("inventory.CraftItemStack")
                    .getMethod("asBukkitCopy", NMSItemStackClass).invoke(null, craftItemStack);

            // TODO: This method of serialization has some issues. Not all the names are the same between versions
            // Make an exception for reeds/melon, they NEED to load in the island chest
            // This code is here SPECIFICALLY to get the default.structure to load properly in all versions
            // Other structures people make NEED to be saved from the version that they will be using so everything loads properly
            if (itemStack.getType() == Material.AIR) {
                if (NBTTagCompound.toString().equals("{id:\"minecraft:sugar_cane\",Count:1b}")) {
                    itemStack = new ItemStack(CompatibleMaterial.SUGAR_CANE.getMaterial(), 1);
                } else if (NBTTagCompound.toString().equals("{id:\"minecraft:melon_slice\",Count:1b}")) {
                    itemStack = new ItemStack(CompatibleMaterial.MELON_SLICE.getMaterial(), 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return itemStack;
    }

    public static String serializeItemStack(ItemStack item) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        try {
            Class<?> NBTTagCompoundClass = NMSUtil.getNMSClass("NBTTagCompound");
            Constructor<?> nbtTagCompoundConstructor = NBTTagCompoundClass.getConstructor();
            Object NBTTagCompound = nbtTagCompoundConstructor.newInstance();
            Object NMSItemStackClass = NMSUtil.getCraftClass("inventory.CraftItemStack")
                    .getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            NMSUtil.getNMSClass("ItemStack").getMethod("save", NBTTagCompoundClass).invoke(NMSItemStackClass,
                    NBTTagCompound);
            NMSUtil.getNMSClass("NBTCompressedStreamTools").getMethod("a", NBTTagCompoundClass, DataOutput.class)
                    .invoke(null, NBTTagCompound, dataOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }
}
