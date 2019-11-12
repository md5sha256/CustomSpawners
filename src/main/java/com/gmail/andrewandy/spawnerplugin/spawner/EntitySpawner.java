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

public class EntitySpawner extends AbstractSpawner {

    private static final int VERSION = 0;

    private final EntityType spawnedType;
    private static final WrapperImpl WRAPPER = WrapperImpl.getInstance();

    public EntitySpawner(Location location, Material material, UUID owner, int delay, EntityType spawnedType) {
        this(location, material, owner, delay, null, spawnedType);
    }

    public EntitySpawner(Location location, Material material, UUID owner, int delay, float spawnChance, EntityType spawnedType) {
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
    public Optional<Gui> getDisplayUI() {
        return Optional.empty();
    }

    public static ItemWrapper<? extends EntitySpawner> getWrapper() {
        return WRAPPER;
    }

    private static class WrapperImpl extends ItemWrapper<EntitySpawner> {

        private static WrapperImpl instance;

        private WrapperImpl() {}

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
