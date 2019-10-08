package com.gmail.andrewandy.spawnerplugin.object;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Location;

public class StackableItemSpawner extends ItemStackSpawner {

    private final int currentSize;
    private final int maxSize;

    public StackableItemSpawner(NBTItem base, int delay, Location location, boolean glowing, int size, int maxSize) {
        super(base, delay, location, glowing);
        this.currentSize = size;
        this.maxSize = maxSize;
    }

    public StackableItemSpawner(NBTItem base, int delay, int amount, Location location, boolean glowing, int size, int maxSize) {
        super(base, delay, amount, location, glowing);
        this.currentSize = size;
        this.maxSize = maxSize;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public int getMaxSize() {
        return maxSize;
    }
}
