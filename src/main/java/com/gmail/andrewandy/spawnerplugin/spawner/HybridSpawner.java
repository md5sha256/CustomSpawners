package com.gmail.andrewandy.spawnerplugin.spawner;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class HybridSpawner extends ItemSpawner {

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

    public HybridSpawner(Location location, Material material, UUID owner, int tickDelay, ItemStack toSpawn, Collection<UUID> peers,Map<EntityType, Float> spawnMap) {
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

        private WrapperImpl() {}

        public static WrapperImpl getInstance() {
            if (instance == null) {
                instance = new WrapperImpl();
            }
            return instance;
        }

        @Override
        public ItemStack toItem(HybridSpawner spawner) {
            return null;
        }

        @Override
        public Optional<OfflineSpawner<HybridSpawner>> fromItem(ItemStack itemStack) {
            return Optional.empty();
        }

        @Override
        public boolean isSpawner(ItemStack itemStack) {
            return false;
        }
    }
}
