package com.gmail.andrewandy.spawnerplugin.spawner.stackable;

import com.gmail.andrewandy.corelib.util.gui.Gui;
import com.gmail.andrewandy.spawnerplugin.spawner.AbstractSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.LivingEntitySpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.OfflineSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.custom.CustomisableSpawner;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EntitySpawner extends AbstractSpawner implements LivingEntitySpawner, StackableSpawner<EntitySpawner>, CustomisableSpawner {

    private static final int VERSION = 0;
    private final int maxSize;
    private final EntityType spawnedType;
    private Collection<OfflineSpawner<EntitySpawner>> stacked = new HashSet<>();

    public EntitySpawner(Location location, Material material, UUID owner, int delay, EntityType spawnedType, int maxSize) {
        super(location, material, owner, delay);
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
        if (!Objects.requireNonNull(spawnedType).isSpawnable() || spawnedType.getEntityClass() == null) {
            throw new IllegalArgumentException("Invalid EntityType.");
        }
        this.spawnedType = spawnedType;
    }

    public EntitySpawner(Location location, Material material, UUID owner, int delay, float spawnChance, EntityType spawnedType, int maxSize) {
        super(location, material, owner, delay, spawnChance);
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
        if (!Objects.requireNonNull(spawnedType).isSpawnable() || spawnedType.getEntityClass() == null) {
            throw new IllegalArgumentException("Invalid EntityType.");
        }
        this.spawnedType = spawnedType;
    }

    public EntitySpawner(Location location, Material material, UUID owner, int delay, Collection<UUID> peers, EntityType spawnedType, int maxSize) {
        super(location, material, owner, delay, peers);
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
        if (!Objects.requireNonNull(spawnedType).isSpawnable() || spawnedType.getEntityClass() == null) {
            throw new IllegalArgumentException("Invalid EntityType.");
        }
        this.spawnedType = spawnedType;
    }

    public static ItemWrapper<? extends EntitySpawner> getWrapper() {
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
    protected void tick() {
        super.tick();
        if (shouldSpawn()) {
            spawnTick(getLocation().getBlock());
        }
    }

    @Override
    public void spawnTick(Block block) {
        if (block == null) {
            return;
        }
        Optional<Block> nearestAirBlock = nearestAirBlock();
        int spawnAmount = 1;
        for (int i = 0; i < size(); i++) {
            if (shouldSpawn()) {
                spawnAmount++;
            }
        }
        assert spawnedType.getEntityClass() != null;
        if (!nearestAirBlock.isPresent()) {
            updateInvalidLocationDisplay("&cUnable to find location to Spawn", Color.RED, super.delay, 1);
            return;
        }
        while (spawnAmount > 0) {
            block.getWorld().spawn(nearestAirBlock.get().getLocation(), spawnedType.getEntityClass());
            spawnAmount --;
        }
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
    public Collection<OfflineSpawner<EntitySpawner>> getStacked() {
        return Collections.unmodifiableCollection(stacked);
    }

    @Override
    public void stack(OfflineSpawner<EntitySpawner> spawner) {
        if (size() + 1 == maxSize) {
            throw new IllegalStateException("Stack is full.");
        }
        stacked.add(spawner);
    }

    @Override
    public void remove(OfflineSpawner<EntitySpawner> spawner) {
        stacked.remove(Objects.requireNonNull(spawner));
    }

    @Override
    public boolean stackAll(Collection<OfflineSpawner<EntitySpawner>> offlineSpawners) {
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
        if (!(o instanceof EntitySpawner)) {
            return false;
        }
        EntitySpawner target = (EntitySpawner) o;
        return target.hashCode() == this.hashCode();
    }

    private static class WrapperImpl extends ItemWrapper<EntitySpawner> {

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
        public ItemStack toItem(EntitySpawner spawner) {
            return null;
        }

        @Override
        public Optional<OfflineSpawner<EntitySpawner>> fromItem(ItemStack itemStack) {
            return Optional.empty();
        }

        @Override
        public Optional<EntitySpawner> place(OfflineSpawner<EntitySpawner> spawner, Location location) {
            return Optional.empty();
        }

        @Override
        public boolean isSpawner(ItemStack itemStack) {
            return false;
        }
    }
}
