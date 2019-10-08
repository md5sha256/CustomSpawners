package com.gmail.andrewandy.spawnerplugin.object;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class StackableItemSpawner extends ItemStackSpawner implements StackableSpawner {

    private final int maxSize;
    private List<OfflineSpawner> stacked;

    public StackableItemSpawner(ItemStack serialized, Location location) throws IllegalAccessException {
        super(serialized, location);
        this.maxSize = -1;
        this.stacked = null;
    }

    public StackableItemSpawner(NBTItem base, int delay, Location location, boolean glowing, int maxSize) {
        super(base, delay, location, glowing);
        this.maxSize = maxSize;
        this.stacked = new ArrayList<>(maxSize);
    }

    public StackableItemSpawner(NBTItem base, int delay, int amount, Location location, boolean glowing, int maxSize) {
        super(base, delay, amount, location, glowing);
        this.maxSize = maxSize;
        this.stacked = new ArrayList<>(maxSize);
    }


    @Override
    public int getSize() {
        return stacked.size();
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public void add(OfflineSpawner spawner) {
        if (isFull()) {
            return;
        }
        stacked.add(Objects.requireNonNull(spawner));
    }

    @Override
    public void addAll(Collection<OfflineSpawner> spawners) {
        if (isFull() || getSize() + spawners.size() > maxSize) {
            return;
        }
        stacked.addAll(spawners);
    }

    @Override
    public void withdraw(OfflineSpawner spawner) {
        stacked.remove(spawner);
    }

    @Override
    public void clear() {
        stacked.clear();
    }

    @Override
    public boolean canStack(OfflineSpawner spawner) {
        return spawner.getOriginalClass().isAssignableFrom(StackableItemSpawner.class)
                && spawner.getOwner() == getOwner() || !getTeamMembers().contains(spawner.getOwner());
    }

    @Override
    public List<OfflineSpawner> getStacked() {
        return new ArrayList<>(stacked);
    }
}
