package com.gmail.andrewandy.spawnerplugin.spawner;

import com.gmail.andrewandy.corelib.util.gui.Gui;
import com.gmail.andrewandy.spawnerplugin.spawner.stackable.StackableEntitySpawner;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class EntitySpawner extends StackableEntitySpawner {

    private static final int VERSION = 0;

    public EntitySpawner(Location location, Material material, UUID owner, int delay, EntityType spawnedType) {
        this(location, material, owner, delay, null, spawnedType);
    }

    public EntitySpawner(Location location, Material material, UUID owner, int delay, float spawnChance, EntityType spawnedType) {
        super(location, material, owner, delay, spawnChance, spawnedType, 1);
    }

    public EntitySpawner(Location location, Material material, UUID owner, int delay, Collection<UUID> peers, EntityType spawnedType) {
        super(location, material, owner, delay, peers, spawnedType, 1);
        if (!spawnedType.isSpawnable()) {
            throw new IllegalArgumentException("Invalid spawnedType");
        }
    }

    public static ItemWrapper<EntitySpawner> getWrapper() {
        return ItemWrapperImpl.getInstance();
    }

    @Override
    public BlockState getAsBlockState() {
        return super.getAsBlockState();
    }

    @Override
    public void initialize() {
        return;
    }

    @Override
    public Optional<Gui> getDisplayUI() {
        return Optional.empty();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    private static class ItemWrapperImpl extends ItemWrapper<EntitySpawner> {

        private static ItemWrapperImpl instance;
        @SuppressWarnings("unchecked")
        ItemWrapper<StackableEntitySpawner> wrapper = (ItemWrapper<StackableEntitySpawner>) StackableEntitySpawner.getWrapper();

        private ItemWrapperImpl() {
        }

        public static ItemWrapperImpl getInstance() {
            if (instance == null) {
                instance = new ItemWrapperImpl();
            }
            return instance;
        }

        @Override
        public ItemStack toItem(EntitySpawner spawner) {
            return wrapper.toItem(spawner);
        }

        @Override
        public Optional<OfflineSpawner<EntitySpawner>> fromItem(ItemStack itemStack) {
            Optional<OfflineSpawner<StackableEntitySpawner>> optional = wrapper.fromItem(itemStack);
            if (optional.isPresent()) {
                OfflineSpawner<StackableEntitySpawner> offlineSpawner = optional.get();
                return Optional.of(new OfflineSpawner<>(EntitySpawner.class, itemStack));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Optional<EntitySpawner> place(OfflineSpawner<EntitySpawner> spawner, Location location) {
            //TODO
            return Optional.empty();
        }

        @Override
        public boolean isSpawner(ItemStack itemStack) {
            return wrapper.isSpawner(itemStack);
        }
    }
}
