package com.gmail.andrewandy.spawnerplugin.listener;

import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.data.DataUtil;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerPlaceEvent;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class BlockPlaceListener implements Listener {
    @EventHandler
    public void blockPlaceListener(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (!block.getType().equals(Material.SPAWNER)) {
            return;
        }
        ItemStack item = event.getItemInHand();
        NBTItem nbtItem = new NBTItem(item);
        if (!nbtItem.hasNBTData() || nbtItem.getString("spawner").isEmpty()) {
            System.out.println(nbtItem.asNBTString());
            return;
        }
        int level = nbtItem.getInteger("level");
        int delay = nbtItem.getInteger("delay");
        int maxLevel = nbtItem.getInteger("maxLevel");
        EntityType type = EntityType.valueOf(nbtItem.getString("entityType"));
        Location location = block.getLocation();
        Spawner spawner = new Spawner(type, delay, level, maxLevel, location);
        SpawnerPlugin.getSpawnerCache().cache(spawner);
        if (DataUtil.loadData(location) == null) {
            DataUtil.saveData(spawner);
        }
        event.getPlayer().getInventory().remove(event.getItemInHand());
        Event spawnerPlaceEvent = new SpawnerPlaceEvent(event.getPlayer(), spawner);
        spawnerPlaceEvent.callEvent();
    }
}
