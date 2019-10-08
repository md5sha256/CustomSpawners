package com.gmail.andrewandy.spawnerplugin.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;

public class SpawnerUtil {

    public static Block createSpawner(Block block, Spawner spawner) {
        if (!block.getType().equals(Material.SPAWNER)) {
            throw new UnsupportedOperationException("Cannot create spawner from" + block.getType());
        }
        CreatureSpawner creatureSpawner = (CreatureSpawner) block;
        creatureSpawner.setDelay(spawner.getDelay());
        creatureSpawner.setSpawnedType(spawner.getSpawnedType());
        return (Block) creatureSpawner;
    }

}
