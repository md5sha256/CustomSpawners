package com.gmail.andrewandy.spawnerplugin.spawner.custom;

import com.gmail.andrewandy.spawnerplugin.spawner.AbstractSpawner;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.UUID;

public abstract class AbstractCustomizableSpawner extends AbstractSpawner {

    public AbstractCustomizableSpawner(Location location, Material material, UUID owner, int delay) {
        super(location, material, owner, delay);
    }

    public AbstractCustomizableSpawner(Location location, Material material, UUID owner, int delay, float spawnChance) {
        super(location, material, owner, delay, spawnChance);
    }

    public AbstractCustomizableSpawner(Location location, Material material, UUID owner, int delay, Collection<UUID> peers) {
        super(location, material, owner, delay, peers);
    }

    /**
     * Represents a fake tick where only spawning functions occurs,
     * no message or errors should originate from this block but
     * instead are called from the original spawner block.
     * This is used for the {@link CustomAreaSpawner} implementations
     * whereby this method is called to 'spawn' at given locations.
     *
     * @param block The block to execute a spawn tick.
     */
    public abstract void spawnTick(Block block);
}
