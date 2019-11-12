package com.gmail.andrewandy.spawnerplugin.spawner;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a spawner where offline spawners can be added to the stack.
 */
public abstract class AbstractStackableSpawner extends AbstractSpawner implements StackableSpawner {

    private Collection<OfflineSpawner<? extends AbstractStackableSpawner>> stacked = new HashSet<>();


    public AbstractStackableSpawner(Location location, Material material, UUID owner, int delay) {
        super(location, material, owner, delay);
    }

    public AbstractStackableSpawner(Location location, Material material, UUID owner, int delay, double spawnChance) {
        super(location, material, owner, delay, spawnChance);
    }

    public AbstractStackableSpawner(Location location, Material material, UUID owner, int delay, Collection<UUID> peers, Collection<OfflineSpawner<? extends AbstractStackableSpawner>> stacked) {
        super(location, material, owner, delay, peers);
        Objects.requireNonNull(stacked);
        if (!canStack(stacked)) {
            throw new IllegalArgumentException("Invalid Spawner Stack. Unable to stack spawner.");
        }
    }
}
