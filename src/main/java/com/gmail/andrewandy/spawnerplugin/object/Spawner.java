package com.gmail.andrewandy.spawnerplugin.object;


import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.Objects;

public class Spawner implements Cloneable {

    private EntityType spawnedType;
    private String identifier;
    private int delay;
    private Location location;
    private int level;
    private int maxLevel;
    private double nextUpgradeCost = 1000;

    public Spawner(EntityType spawnedType, int delay, int level, int maxLevel, Location location) {
        Objects.requireNonNull(spawnedType);
        this.spawnedType = spawnedType;
        this.identifier = asIdentifier(location);
        this.delay = delay;
        this.location = location;
        this.level = level;
        this.maxLevel = maxLevel;
    }

    public static String asIdentifier(Location location) {
        Objects.requireNonNull(location);
        Objects.requireNonNull(location.getWorld(), "Null world!");
        return location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public EntityType getSpawnedType() {
        return spawnedType;
    }

    public void setSpawnedType(EntityType spawnedType) {
        this.spawnedType = spawnedType;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        if (level > maxLevel) {
            this.level = maxLevel;
            return;
        }
        this.level = level;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Spawner clone() {
        return new Spawner(spawnedType, delay, level, maxLevel, location.clone());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Spawner)) {
            return false;
        }
        Spawner target = (Spawner) obj;
        if (target == this) {
            return true;
        } else {
            return target.getIdentifier().equalsIgnoreCase(this.identifier) && target.getLevel() == level && target.getDelay() == delay
                    && target.getLocation().equals(location) && target.getSpawnedType() == spawnedType;
        }
    }

    public boolean equalsIgnoreLocation(Spawner target) {
        return target.getLevel() == level && target.getDelay() == delay && target.getSpawnedType() == spawnedType;
    }

    /**
     * Get the upgrade cost for the next level.
     *
     * @return The cost to upgrade, -1 if max level has been reached.
     */
    public double getNextUpgradeCost() {
        return nextUpgradeCost;
    }
}
