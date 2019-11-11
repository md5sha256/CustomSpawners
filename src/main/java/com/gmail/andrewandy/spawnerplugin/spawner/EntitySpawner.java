package com.gmail.andrewandy.spawnerplugin.spawner;

import com.gmail.andrewandy.corelib.util.gui.Gui;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class EntitySpawner extends Spawner {

    private final EntityType spawnedType;

    public EntitySpawner(Location location, Material material, UUID owner, int delay, EntityType spawnedType) {
        this(location, material, owner, delay, null, spawnedType);
    }

    public EntitySpawner(Location location, Material material, UUID owner, int delay, double spawnChance, EntityType spawnedType) {
        super(location, material, owner, delay, spawnChance);
        Objects.requireNonNull(spawnedType);
        if (!spawnedType.isSpawnable()) {
            throw new IllegalArgumentException("Invalid spawnedType");
        }
        this.spawnedType = spawnedType;
    }

    public EntitySpawner(Location location, Material material, UUID owner, int delay, Collection<UUID> peers, EntityType spawnedType) {
        super(location, material, owner, delay, peers);
        if (!spawnedType.isSpawnable()) {
            throw new IllegalArgumentException("Invalid spawnedType");
        }
        this.spawnedType = spawnedType;
    }

    public EntityType getSpawnedType() {
        return spawnedType;
    }

    @Override
    public BlockState getAsBlockState() {
        throw new UnsupportedOperationException("Unimplemented.");
    }

    @Override
    public void initialize() {
        return;
    }

    @Override
    boolean isSpawner(ItemStack itemStack) {
        throw new UnsupportedOperationException("Unimplemented.");
    }

    @Override
    public Optional<Gui> getDisplayUI() {
        return Optional.empty();
    }
}
