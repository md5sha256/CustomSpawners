package com.gmail.andrewandy.spawnerplugin.spawner;

import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.spawner.custom.CustomAreaSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.custom.CustomisableSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.stackable.EntitySpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.stackable.ItemSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.stackable.PotionEffectSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.stackable.StackableSpawner;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Function;

public final class Spawners {

    public static final String DEFAULT_META_KEY = "CustomSpawner";

    public static LivingEntitySpawner singleStackEntitySpawner(Location location, Material baseBlock, UUID owner, int delay, float spawnChance, EntityType spawnedType) {
        return new EntitySpawner(location, baseBlock, owner, delay, spawnChance, spawnedType, 1);
    }

    public static ItemStackSpawner singleStackItemSpawner(Location location, Material baseBlock, UUID owner, int delay, float spawnChance, ItemStack toSpawn) {
        return new ItemSpawner(location, baseBlock, owner, delay, spawnChance, toSpawn, 1);
    }

    public static com.gmail.andrewandy.spawnerplugin.spawner.stackable.PotionEffectSpawner singleStackPotionEffectSpawner(Location location, Material baseBlock, UUID owner, int delay, float spawnChance, PotionEffect potionEffect, boolean lingering) {
        return new PotionEffectSpawner(location, baseBlock, owner, delay, spawnChance, potionEffect, lingering, 1);
    }

    public static <T extends AbstractSpawner & CustomisableSpawner & StackableSpawner<T>>
    CustomAreaSpawner<T> singleStackCustomAreaSpawner(T original, Function<Block, Block[]> spawnLocationFunction) {
        return new CustomAreaSpawner<>(original, spawnLocationFunction, 1);
    }

    public static Optional<Spawner.ItemWrapper> getWrapper(ItemStack spawner) {
        if (spawner == null) {
            return Optional.empty();
        }
        NBTItem nbtItem = new NBTItem(spawner);
        String rawClass = nbtItem.getString("class");
        String rawWrappedClass = nbtItem.getString("wrappedClass");
        if (rawClass == null) {
            return Optional.empty();
        }
        try {
            Class<?> clazz = Class.forName(rawClass);
            if (!clazz.isAssignableFrom(AbstractSpawner.class)) {
                return Optional.empty();
            }
            Class<? extends AbstractSpawner> casted = clazz.asSubclass(AbstractSpawner.class);
            if (rawWrappedClass != null) {
                Class<?> wrapper = Class.forName(rawWrappedClass);
                if (!wrapper.isAssignableFrom(CustomAreaSpawner.class)) {
                    throw new IllegalStateException();
                }
                return Optional.of((Spawner.ItemWrapper) casted.getMethod("getWrapper").invoke(null));
            } else {
                return Optional.of((Spawner.ItemWrapper) clazz.getMethod("getWrapper").invoke(null));
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return Optional.empty();
    }

    public static SpawnerManager defaultManager() {
        return ManagerImpl.getInstance();
    }

    private static class ManagerImpl implements SpawnerManager {
        private static ManagerImpl instance;

        public static ManagerImpl getInstance() {
            if (instance == null) {
                instance = new ManagerImpl();
            }
            return instance;
        }

        private ManagerImpl() {
        }

        private Map<Location, BukkitTask> taskMap = new HashMap<>();

        @Override
        public BukkitTask registerSpawner(AbstractSpawner spawner) {
            Objects.requireNonNull(spawner);
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(SpawnerPlugin.getInstance(), spawner::tick, 0, spawner.getDelay());
            taskMap.computeIfPresent(spawner.getLocation(), (loc, currentTask) -> {
                if (!currentTask.isCancelled()) {
                    currentTask.cancel();
                }
                return task;
            });
            taskMap.putIfAbsent(spawner.getLocation(), task);
            return task;
        }

        @Override
        public void unregisterSpawner(Location spawner) {
            Objects.requireNonNull(Objects.requireNonNull(spawner).getWorld());
            if (taskMap.containsKey(spawner)) {
                BukkitTask current = taskMap.get(spawner);
                if (!current.isCancelled()) {
                    Optional<AbstractSpawner> optionalAbstractSpawner = getFromLocation(spawner);
                    //Save the data to blockstate forcefully and kill the shulker display before clearing.
                    optionalAbstractSpawner.ifPresent(abstractSpawner -> {
                        abstractSpawner.updateBlockState();
                        abstractSpawner.clearShulkerDisplay();
                    });
                    current.cancel();
                }
                taskMap.remove(spawner);
            }
        }

        @Override
        public Collection<Location> getRegisteredSpawners() {
            return Collections.unmodifiableCollection(taskMap.keySet());
        }

        @Override
        public Optional<AbstractSpawner> getFromLocation(Location location) {
            Objects.requireNonNull(Objects.requireNonNull(location).getWorld());
            if (!taskMap.containsKey(location)) {
                return Optional.empty();
            }
            Block block = location.getBlock();
            Optional<MetadataValue> optionalMetadataValue = block.getMetadata(DEFAULT_META_KEY).stream().findAny();
            if (!optionalMetadataValue.isPresent()) {
                return Optional.empty();
            }
            Object rawObj = optionalMetadataValue.get().value();
            if (!(rawObj instanceof ItemStack)) {
                return Optional.empty();
            }
            ItemStack itemStack = (ItemStack) rawObj;
            Optional<Spawner.ItemWrapper> optional = Spawners.getWrapper(itemStack.clone());
            if (!optional.isPresent()) {
                return Optional.empty();
            }
            Spawner.ItemWrapper raw = optional.get();
            Optional<?> optionalOfflineSpawner = raw.fromItem(itemStack);
            if (!optionalOfflineSpawner.isPresent()) {
                return Optional.empty();
            }
            Object o = optionalOfflineSpawner.get();
            if (!(o instanceof OfflineSpawner)) {
                throw new IllegalStateException();
            }
            OfflineSpawner offlineSpawner = (OfflineSpawner) o;
            if (!offlineSpawner.isValidSpawner()) {
                return Optional.empty();
            }
            return raw.toLiveAtLocation(offlineSpawner, location);
        }
    }

}
