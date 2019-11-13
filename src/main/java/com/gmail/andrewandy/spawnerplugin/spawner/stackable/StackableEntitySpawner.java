package com.gmail.andrewandy.spawnerplugin.spawner.stackable;

import com.gmail.andrewandy.corelib.util.gui.Gui;
import com.gmail.andrewandy.spawnerplugin.spawner.AbstractSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.LivingEntitySpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.OfflineSpawner;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class StackableEntitySpawner extends AbstractSpawner implements LivingEntitySpawner, StackableSpawner<StackableEntitySpawner> {

    private static final int VERSION = 0;
    private final int maxSize;
    private final EntityType spawnedType;
    private Collection<OfflineSpawner<StackableEntitySpawner>> stacked = new HashSet<>();

    public StackableEntitySpawner(Location location, Material material, UUID owner, int delay, EntityType spawnedType, int maxSize) {
        super(location, material, owner, delay);
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
        if (!Objects.requireNonNull(spawnedType).isSpawnable()) {
            throw new IllegalArgumentException("Invalid EntityType.");
        }
        this.spawnedType = spawnedType;
    }

    public StackableEntitySpawner(Location location, Material material, UUID owner, int delay, float spawnChance, EntityType spawnedType, int maxSize) {
        super(location, material, owner, delay, spawnChance);
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
        if (!Objects.requireNonNull(spawnedType).isSpawnable()) {
            throw new IllegalArgumentException("Invalid EntityType.");
        }
        this.spawnedType = spawnedType;
    }

    public StackableEntitySpawner(Location location, Material material, UUID owner, int delay, Collection<UUID> peers, EntityType spawnedType, int maxSize) {
        super(location, material, owner, delay, peers);
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
        if (!Objects.requireNonNull(spawnedType).isSpawnable()) {
            throw new IllegalArgumentException("Invalid EntityType.");
        }
        this.spawnedType = spawnedType;
    }

    public static ItemWrapper<? extends StackableEntitySpawner> getWrapper() {
        return WrapperImpl.getInstance();
    }

    @Override
    public BlockState getAsBlockState() {
        throw new UnsupportedOperationException("Unimplemented.");
    }

    @Override
    public void initialize() {
    }

    @Override
    public Optional<Gui> getDisplayUI() {
        return Optional.empty();
    }

    @Override
    public int maxSize() {
        return maxSize;
    }

    @Override
    public Collection<OfflineSpawner<StackableEntitySpawner>> getStacked() {
        return Collections.unmodifiableCollection(stacked);
    }

    @Override
    public void stack(OfflineSpawner<StackableEntitySpawner> spawner) {
        if (size() + 1 == maxSize) {
            throw new IllegalStateException("Stack is full.");
        }
        stacked.add(spawner);
    }

    @Override
    public void remove(OfflineSpawner<StackableEntitySpawner> spawner) {
        stacked.remove(Objects.requireNonNull(spawner));
    }

    @Override
    public boolean stackAll(Collection<OfflineSpawner<StackableEntitySpawner>> offlineSpawners) {
        if (!canStack(Objects.requireNonNull(offlineSpawners))) {
            return false;
        }
        return stacked.addAll(offlineSpawners);
    }

    @Override
    public EntityType getSpawnedType() {
        return spawnedType;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * super.hashCode();
        hash = hash * maxSize;
        hash = hash * spawnedType.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof StackableEntitySpawner)) {
            return false;
        }
        StackableEntitySpawner target = (StackableEntitySpawner) o;
        return target.hashCode() == this.hashCode();
    }

    private static class WrapperImpl extends ItemWrapper<StackableEntitySpawner> {

        private static WrapperImpl instance;

        private WrapperImpl() {
        }

        public static WrapperImpl getInstance() {
            if (instance == null) {
                instance = new WrapperImpl();
            }
            return instance;
        }

        @Override
        public ItemStack toItem(StackableEntitySpawner spawner) {
            return null;
        }

        @Override
        public Optional<OfflineSpawner<StackableEntitySpawner>> fromItem(ItemStack itemStack) {
            return Optional.empty();
        }

        @Override
        public Optional<StackableEntitySpawner> place(OfflineSpawner<StackableEntitySpawner> spawner, Location location) {
            return Optional.empty();
        }

        @Override
        public boolean isSpawner(ItemStack itemStack) {
            return false;
        }
    }
}
