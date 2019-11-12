package com.gmail.andrewandy.spawnerplugin.spawner;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class HybridSpawner extends ItemSpawner {
    private static final int VERSION = 0;

    private Map<EntityType, Float> spawnMap;
    private static final ItemWrapper<HybridSpawner> WRAPPER = WrapperImpl.getInstance();

    public HybridSpawner(Location location, Material material, UUID owner, int tickDelay, float spawnChance, ItemStack toSpawn, Map<EntityType, Float> spawnMap) {
        super(location, material, owner, tickDelay, spawnChance, toSpawn);
        if (!validSpawnMap(spawnMap)) {
            throw new IllegalArgumentException("Invalid SpawnMap.");
        }
        this.spawnMap = spawnMap;
    }

    public HybridSpawner(Location location, Material material, UUID owner, int tickDelay, ItemStack toSpawn, Map<EntityType, Float> spawnMap) {
        super(location, material, owner, tickDelay, toSpawn);
        if (!validSpawnMap(spawnMap)) {
            throw new IllegalArgumentException("Invalid SpawnMap.");
        }
        this.spawnMap = spawnMap;
    }

    public HybridSpawner(Location location, Material material, UUID owner, int tickDelay, ItemStack toSpawn, Collection<UUID> peers, Map<EntityType, Float> spawnMap) {
        super(location, material, owner, tickDelay, toSpawn, peers);
        if (!validSpawnMap(spawnMap)) {
            throw new IllegalArgumentException("Invalid SpawnMap.");
        }
        this.spawnMap = spawnMap;
    }

    public static ItemWrapper<? extends HybridSpawner> getWrapper() {
        return WRAPPER;
    }

    //TODO

    public boolean validSpawnMap(Map<EntityType, Float> spawnMap) {
        Objects.requireNonNull(spawnMap);
        float percentage = 0.00F;
        for (Map.Entry<EntityType, Float> entry : spawnMap.entrySet()) {
            if (!entry.getKey().isSpawnable()) {
                return false;
            }
            percentage += entry.getValue();
        }
        //Check for floating-point errors within 5dp
        return percentage > 0.99999 && percentage < 1.000001;
    }

    private static class WrapperImpl extends ItemWrapper<HybridSpawner> {

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
        @SuppressWarnings("unchecked")
        public ItemStack toItem(HybridSpawner spawner) {
            Objects.requireNonNull(spawner);
            ItemWrapper<ItemSpawner> wrapper = (ItemWrapper<ItemSpawner>) ItemSpawner.getWrapper();
            NBTItem nbtItem = new NBTItem(wrapper.toItem(spawner));
            nbtItem.setString("class", HybridSpawner.class.getName());
            nbtItem.setInteger("classVersion", VERSION);
            //TODO Implement methods of EntitySpawner.
        }

        @Override
        @SuppressWarnings("unchecked")
        public Optional<OfflineSpawner<HybridSpawner>> fromItem(ItemStack itemStack) {
            Optional<OfflineSpawner<ItemSpawner>> rawOptional = ((ItemWrapper<ItemSpawner>) ItemSpawner.getWrapper()).fromItem(itemStack);
            if (!rawOptional.isPresent()) {
                return Optional.empty();
            }
            return Optional.of(new OfflineSpawner<>(HybridSpawner.class, itemStack));
        }

        @Override
        public Optional<HybridSpawner> place(OfflineSpawner<HybridSpawner> spawner, Location location) {
            return Optional.empty();
        }

        @Override
        public boolean isSpawner(ItemStack itemStack) {
            fromItem(itemStack).isPresent()
        }
    }
}
