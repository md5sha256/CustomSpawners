package com.gmail.andrewandy.skyblockspawners.object;


import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class Spawner {

    private EntityType spawnedType;
    private String identifier;
    private int delay;
    private Location location;
    private int level;
    private int maxLevel;

    public Spawner(EntityType spawnedType, int delay, int level, int maxLevel, Location location) {
        if (location.getWorld() == null || location.getWorld().getName().isEmpty()) {
            throw new IllegalStateException("The world is null!");
        }
        this.spawnedType = spawnedType;
        this.identifier = location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
        this.delay = delay;
        this.location = location;
        this.level = level;
        this.maxLevel = maxLevel;
    }

    public String getUniqueIdentifier() {
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
        this.level = level;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }
}
