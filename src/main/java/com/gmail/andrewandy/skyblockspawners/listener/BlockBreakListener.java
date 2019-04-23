package com.gmail.andrewandy.skyblockspawners.listener;

import com.gmail.andrewandy.skyblockspawners.SkyblockSpawnerBukkit;
import com.gmail.andrewandy.skyblockspawners.object.Spawner;
import com.gmail.andrewandy.skyblockspawners.util.Common;
import de.tr7zw.itemnbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;

/*
Well aware this is inefficient. Will be fixed.
 */

public class BlockBreakListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        System.out.println(event.isCancelled());
        Common.log(Level.INFO, "Spawner was broken.");
        if (!(event.getBlock() instanceof CreatureSpawner)) {
            Common.log(Level.INFO, "test");
            return;
        }
        Spawner spawner = SkyblockSpawnerBukkit.getSpawnerManager().getSpawnerAtLocation(event.getBlock().getWorld(), event.getBlock().getLocation());
        SkyblockSpawnerBukkit.getSpawnerManager().removeSpawner(spawner);
        ItemStack item = new ItemStack(Material.SPAWNER);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("&e" + Common.capitalise(spawner.getSpawnedType().name().toLowerCase()) + "&e Spawner");
        itemMeta.setLore(Arrays.asList(
                Common.colourise(""),
                Common.colourise("&b&lInformation:"),
                Common.colourise("  &7-&a Mob: " + Common.capitalise(spawner.getSpawnedType().name().toLowerCase())),
                Common.colourise("  &7-&e Level: " + spawner.getLevel())
                ));
        item.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("entityType", spawner.getSpawnedType().name());
        nbtItem.setInteger("level", spawner.getLevel());
        nbtItem.setInteger("delay", spawner.getDelay());
        nbtItem.setInteger("maxLevel", spawner.getMaxLevel());
        ItemStack finalItem = nbtItem.getItem();
        World world = spawner.getLocation().getWorld();
        world.dropItemNaturally(spawner.getLocation(), finalItem);

    }


}
