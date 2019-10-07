package com.gmail.andrewandy.spawnerplugin.listener;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerPlaceEvent;
import com.gmail.andrewandy.spawnerplugin.object.Spawner;
import com.gmail.andrewandy.spawnerplugin.util.Common;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SpawnerPlaceListener implements Listener {

    @EventHandler
    public void spawnerPlaceListener(SpawnerPlaceEvent event) {
        Player player = event.getPlayer();
        EntityType type = event.getSpawner().getSpawnedType();
        int level = event.getSpawner().getLevel();

        Spawner spawner = event.getSpawner();
        Block block = event.getSpawner().getLocation().getBlock();
        CreatureSpawner spawnerBlock = (CreatureSpawner) block.getState();
        spawnerBlock.setSpawnedType(spawner.getSpawnedType());
        //TODO fix delays.
        spawnerBlock.setDelay(spawner.getDelay());
        spawnerBlock.update(true);
        SpawnerPlugin.getSpawnerCache().cache(spawner);
        Common.tell(player, "&bYou have placed a level &e" + level + " &a&n" + Common.capitalise(type.name().toLowerCase()) + "&b spawner!");
    }

}
