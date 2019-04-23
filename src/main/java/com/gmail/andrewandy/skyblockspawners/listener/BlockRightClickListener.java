package com.gmail.andrewandy.skyblockspawners.listener;

import com.gmail.andrewandy.skyblockspawners.SkyblockSpawnerBukkit;
import com.gmail.andrewandy.skyblockspawners.SkyblockSpawners;
import com.gmail.andrewandy.skyblockspawners.event.SpawnerRightClickEvent;
import com.gmail.andrewandy.skyblockspawners.object.Spawner;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockRightClickListener implements Listener {

    @EventHandler
    public void onBlockRightClick(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || !(event.getClickedBlock().getState() instanceof CreatureSpawner)) {
            return;
        }

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        Spawner spawner = SkyblockSpawnerBukkit.getSpawnerManager().getSpawnerAtLocation(block.getWorld(), block.getLocation());
        if (spawner == null) {
            System.out.println("Spawner is null");
            return;
        }
        Event spawnerRightClickEvent = new SpawnerRightClickEvent(player, spawner);
        spawnerRightClickEvent.callEvent();
    }
}
