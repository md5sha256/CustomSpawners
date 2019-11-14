package com.gmail.andrewandy.spawnerplugin.spawner.stackable;

import com.gmail.andrewandy.corelib.util.gui.Gui;
import com.gmail.andrewandy.spawnerplugin.spawner.AbstractSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.OfflineSpawner;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PotionEffectSpawner extends AbstractSpawner implements StackableSpawner<PotionEffectSpawner> {

    private final PotionEffect spawnedEffect;
    private final boolean lingering;
    private final int maxSize;

    public PotionEffectSpawner(Location location, Material material, UUID owner, int delay, PotionEffect spawnedEffect, boolean lingering, int maxSize) {
        super(location, material, owner, delay);
        this.spawnedEffect = Objects.requireNonNull(spawnedEffect);
        this.lingering = lingering;
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
    }

    public PotionEffectSpawner(Location location, Material material, UUID owner, int delay, float spawnChance, PotionEffect spawnedEffect, boolean lingering, int maxSize) {
        super(location, material, owner, delay, spawnChance);
        this.spawnedEffect = Objects.requireNonNull(spawnedEffect);
        this.lingering = lingering;
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
    }

    public PotionEffectSpawner(Location location, Material material, UUID owner, int delay, Collection<UUID> peers, PotionEffect spawnedEffect, boolean lingering, int maxSize) {
        super(location, material, owner, delay, peers);
        this.spawnedEffect = Objects.requireNonNull(spawnedEffect);
        this.lingering = lingering;
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
    }

    @Override
    public BlockState getAsBlockState() {
        return null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public Optional<Gui> getDisplayUI() {
        return Optional.empty();
    }

    @Override
    public Collection<OfflineSpawner<PotionEffectSpawner>> getStacked() {
        return null;
    }

    @Override
    public void stack(OfflineSpawner<PotionEffectSpawner> spawner) {

    }

    @Override
    public void remove(OfflineSpawner<PotionEffectSpawner> spawner) {

    }

    @Override
    public int maxSize() {
        return 0;
    }

    @Override
    public boolean stackAll(Collection<OfflineSpawner<PotionEffectSpawner>> offlineSpawners) {
        return false;
    }
}
