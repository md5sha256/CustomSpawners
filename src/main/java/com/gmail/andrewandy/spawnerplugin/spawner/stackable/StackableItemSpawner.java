package com.gmail.andrewandy.spawnerplugin.spawner.stackable;

import com.gmail.andrewandy.spawnerplugin.spawner.ItemSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.OfflineSpawner;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

//TODO Rework like StackableEntitySpawner
public final class StackableItemSpawner extends ItemSpawner implements StackableSpawner<StackableItemSpawner> {

    private final int maxSize;
    private Collection<OfflineSpawner<StackableItemSpawner>> stacked = new HashSet<>();

    public StackableItemSpawner(Location location, Material material, UUID owner, int tickDelay, float spawnChance, ItemStack toSpawn, int maxSize) {
        super(location, material, owner, tickDelay, spawnChance, toSpawn);
        if (maxSize < 1) {
            throw new IllegalArgumentException("Max size must be greater than 0.");
        }
        this.maxSize = maxSize;
    }

    public StackableItemSpawner(Location location, Material material, UUID owner, int tickDelay, ItemStack toSpawn, int maxSize) {
        super(location, material, owner, tickDelay, toSpawn);
        if (maxSize < 1) {
            throw new IllegalArgumentException("Max size must be greater than 0.");
        }
        this.maxSize = maxSize;
    }

    public StackableItemSpawner(Location location, Material material, UUID owner, int tickDelay, ItemStack toSpawn, Collection<UUID> peers, int maxSize) {
        super(location, material, owner, tickDelay, toSpawn, peers);
        if (maxSize < 1) {
            throw new IllegalArgumentException("Max size must be greater than 0.");
        }
        this.maxSize = maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public Collection<OfflineSpawner<StackableItemSpawner>> getStacked() {
        return Collections.unmodifiableCollection(stacked);
    }

    @Override
    public void stack(OfflineSpawner<StackableItemSpawner> spawner) {
        if (isFull()) {
            return;
        }
        stacked.add(spawner);
    }

    @Override
    public void remove(OfflineSpawner<StackableItemSpawner> spawner) {
        stacked.remove(spawner);
    }

    @Override
    public int maxSize() {
        return maxSize;
    }

    @Override
    public boolean stackAll(Collection<OfflineSpawner<StackableItemSpawner>> collection) {
        if (!canStack(Objects.requireNonNull(collection))) {
            return false;
        }
        return stacked.addAll(collection);
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = hash * super.hashCode();
        hash = hash * maxSize;
        hash = hash * stacked.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof StackableItemSpawner)) {
            return false;
        }
        StackableItemSpawner target = (StackableItemSpawner) o;
        return target.hashCode() == this.hashCode();
    }
}
