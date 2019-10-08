package com.gmail.andrewandy.spawnerplugin.listener;

import com.gmail.andrewandy.spawnerplugin.data.DataUtil;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerBreakEvent;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerRightClickEvent;
import com.gmail.andrewandy.spawnerplugin.object.Spawner;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockInteractListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

    }

    @EventHandler
    public void onSpawnerBreak(SpawnerBreakEvent event) {

    }

    public void onBlockInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        Block clicked = event.getClickedBlock();
        assert clicked != null;
        String testLocation = Spawner.getTestIdentifier(clicked.getLocation());
        if (!DataUtil.isSaved(testLocation)) {
            return;
        } else {
            Spawner spawner = DataUtil.loadData(clicked.getLocation());
            SpawnerRightClickEvent target = new SpawnerRightClickEvent(event.getPlayer(), spawner);
            target.callEvent();
        }
    }

}
