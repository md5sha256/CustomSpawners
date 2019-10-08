package com.gmail.andrewandy.spawnerplugin.listener;

import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.data.DataUtil;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerRightClickEvent;
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
        if (event.getClickedBlock() == null) {
            return;
        }
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || !(event.getClickedBlock().getState() instanceof CreatureSpawner)) {
            return;
        }
        Player player = event.getPlayer();

        Spawner spawner = SpawnerPlugin.getSpawnerCache().getFromCache(Spawner.asIdentifier(event.getClickedBlock().getLocation()));
        if (spawner == null) {
            spawner = DataUtil.loadData(event.getClickedBlock().getLocation());
            System.out.println("Spawner is null");
        }
        if (spawner == null) {
            return;
        }
        Event spawnerRightClickEvent = new SpawnerRightClickEvent(player, spawner);
        spawnerRightClickEvent.callEvent();
    }
}
