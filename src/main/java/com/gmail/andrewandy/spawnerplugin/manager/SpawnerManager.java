package com.gmail.andrewandy.spawnerplugin.manager;

import com.gmail.andrewandy.spawnerplugin.object.Spawner;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class SpawnerManager {

    public Set<ItemStack> registeredItems = new HashSet<>();
    private Set<Spawner> registeredSpawners = new HashSet<>();
    private Set<Spawner> toPurge = new HashSet<>();

    public void registerSpawner(Spawner spawner) {
        if (registeredSpawners.contains(spawner)) {
            System.out.println("already contains.");
        }
        registeredSpawners.add(spawner);
    }

    public void removeSpawner(Spawner spawner) {
        registeredSpawners.remove(spawner);
        toPurge.add(spawner);
    }

    public Set<Spawner> getRegisteredSpawners() {
        return this.registeredSpawners;
    }

    public Spawner getSpawnerByIdentifier(String identifier) {
        for (Spawner spawner : registeredSpawners) {
            if (!spawner.getIdentifier().equals(identifier)) {
                continue;
            }
            return spawner;
        }
        return null;
    }

    public Spawner getSpawnerAtLocation(World world, Location location) {
        for (Spawner spawner : registeredSpawners) {
            if (!spawner.getLocation().getWorld().equals(world) || !spawner.getLocation().equals(location)) {
                continue;
            }
            return spawner;
        }
        return null;
    }

    public Set<ItemStack> getRegisteredItems() {
        return this.registeredItems;
    }

    public void removeItem(ItemStack item) {
        registeredItems.remove(item);
    }

    public void registerItem(ItemStack item) {
        registeredItems.remove(item);
    }

    public Set<Spawner> getPurge() {
        return this.toPurge;
    }

}
