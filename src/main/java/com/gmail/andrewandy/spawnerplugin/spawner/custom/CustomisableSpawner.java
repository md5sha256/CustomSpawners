package com.gmail.andrewandy.spawnerplugin.spawner.custom;

import org.bukkit.block.Block;

public interface CustomisableSpawner {

    /**
     * Represents a fake tick where only spawning functions occurs,
     * no message or errors should originate from this block but
     * instead are called from the original spawner block.
     * This is used for the {@link CustomAreaSpawner} implementations
     * whereby this method is called to 'spawn' at given locations.
     *
     * @param block The block to execute a spawn tick.
     */
    void spawnTick(Block block);

}
