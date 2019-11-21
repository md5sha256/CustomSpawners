package com.gmail.andrewandy.spawnerplugin.spawner;

import com.gmail.andrewandy.corelib.util.Common;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.spawner.custom.CustomAreaSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.custom.CustomisableSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.data.SpawnerData;
import com.gmail.andrewandy.spawnerplugin.spawner.stackable.EntitySpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.stackable.ItemSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.stackable.PotionEffectSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.stackable.StackableSpawner;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
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

    public static <U extends AbstractSpawner> boolean hasSpecificWrapper(ItemStack spawner, Class<U> targetClass) {
        if (spawner == null || targetClass == null) {
            return false;
        }
        NBTItem nbtItem = new NBTItem(spawner);
        String rawClass = nbtItem.getString("class");
        if (rawClass == null) {
            return false;
        }
        try {
            Class<?> clazz = Class.forName(rawClass);
            return Common.classEquals(clazz, targetClass);
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    public static <U extends AbstractSpawner & CustomisableSpawner, T extends CustomAreaSpawner> boolean hasSpecificWrapper(ItemStack spawner, Class<U> targetClass, Class<T> targetCustomWrapper) {
        if (spawner == null) return false;
        NBTItem nbtItem = new NBTItem(spawner);
        String rawClass = nbtItem.getString("class");
        String rawWapperClass = nbtItem.getString("wrapperClass");
        if (rawClass == null || rawWapperClass == null) {
            return false;
        }
        try {
            Class<?> rawClazz = Class.forName(rawClass);
            Class<?> rawWrapperClazz = Class.forName(rawWapperClass);
            return targetClass.isAssignableFrom(rawClazz) && targetCustomWrapper.isAssignableFrom(rawWrapperClazz);
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    /**
     * Get the wrapper for any spawner which is a type of {@link CustomAreaSpawner}
     *
     * @param spawner      The ItemStack version of the spawner.
     * @param spawnerClass The class of the CustomWrapper (In this case {@link CustomAreaSpawner};
     * @param wrappedClass The class of the internally wrapped spawner. {@link CustomAreaSpawner#getWrappedSpawner()}
     * @param <U>          The type of internally wrapped spawner.
     * @param <T>          The type of the wrapper class.
     * @return Returns a populated optional if a custom wrapper was found. An empty one if, no wrapper class was found, a mis-match of the spawner class,
     * or if the wrapper class does not hide the {@link CustomAreaSpawner#getSpecificWrapper(Class)} (ItemStack, Class, Class)}.
     * @see CustomisableSpawner
     */
    @SuppressWarnings("unchecked")
    public static <U extends AbstractSpawner & CustomisableSpawner, T extends CustomAreaSpawner>
    Optional<Spawner.ItemWrapper<CustomAreaSpawner<U>>> getCustomWrapper(ItemStack spawner, Class<T> spawnerClass, Class<U> wrappedClass) {
        if (spawner == null || spawnerClass == null || wrappedClass == null) {
            return Optional.empty();
        }
        NBTItem nbtItem = new NBTItem(spawner);
        String rawClass = nbtItem.getString("class");
        String rawWrappedClass = nbtItem.getString("wrappedClass");
        if (rawClass == null) {
            return Optional.empty();
        }
        try {
            Class<?> rawClazz = Class.forName(rawClass);
            if (!spawnerClass.isAssignableFrom(rawClazz)) {
                return Optional.empty();
            } else {
                if (rawWrappedClass == null) {
                    return Optional.empty();
                }
                return Optional.of((Spawner.ItemWrapper<CustomAreaSpawner<U>>) spawnerClass.getMethod("getSpecificWrapper", Class.class).invoke(null, wrappedClass));
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return Optional.empty();

    }

    /**
     * Attempts to get a wrapper of a spawner which extends {@link AbstractSpawner}. This method does not check for wrapper classes,
     *
     * @param spawner The ItemStack version of the spawner. {@link Spawner.ItemWrapper#toItem(AbstractSpawner)}
     * @param clazz   The class object of the spawner.
     * @param <T>     The generic type of the spawner, which must extend AbstractSpawner.
     * @return Returns a populated optional if the wrapper for the given class was found and matches the targeted class. If the class
     * An empty spawner if, the target class does not hide the {@link AbstractSpawner#getWrapper()}, or provides an invalid wrapper - or
     * if the ItemStack is invalid.
     * @see #getCustomWrapper(ItemStack, Class, Class) if you would like to get the custom wrapper.
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractSpawner> Optional<Spawner.ItemWrapper<T>> getWrapper(ItemStack spawner, Class<T> clazz) {
        if (spawner == null || clazz == null) {
            return Optional.empty();
        }
        NBTItem nbtItem = new NBTItem(spawner);
        String rawClass = nbtItem.getString("class");
        if (rawClass == null) {
            return Optional.empty();
        }
        try {
            Class<?> rawClazz = Class.forName(rawClass);
            if (!clazz.isAssignableFrom(rawClazz)) {
                return Optional.empty();
            }
            Spawner.ItemWrapper<? extends AbstractSpawner> wrapper = T.getWrapper();
            //The wrapper here should be of type ItemWrapper<T>
            return Optional.of((Spawner.ItemWrapper<T>) wrapper);
        } catch (ReflectiveOperationException | ClassCastException ignored) {
        }
        return Optional.empty();
    }

    public static SpawnerManager defaultManager() {
        return ManagerImpl.getInstance();
    }

    private static class ManagerImpl implements SpawnerManager {
        private static ManagerImpl instance;
        private Map<Location, BukkitTask> taskMap = new HashMap<>();

        private ManagerImpl() {
        }

        public static ManagerImpl getInstance() {
            if (instance == null) {
                instance = new ManagerImpl();
            }
            return instance;
        }

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
            taskMap.computeIfAbsent(spawner.getLocation(), (loc) -> {
                SpawnerData.writeData(spawner.getLocation());
                return task;
            });
            return task;
        }

        @Override
        public void unregisterSpawner(Location spawner) {
            unregisterSpawner(spawner, false);
        }

        public void unregisterSpawner(Location spawner, boolean clearData) {
            Objects.requireNonNull(Objects.requireNonNull(spawner).getWorld());
            if (taskMap.containsKey(spawner)) {
                BukkitTask current = taskMap.get(spawner);
                if (!current.isCancelled()) {
                    Optional<AbstractSpawner> optionalAbstractSpawner = getFromLocation(spawner);
                    //Save the data to block forcefully and kill the shulker display before clearing.
                    optionalAbstractSpawner.ifPresent(abstractSpawner -> {
                        abstractSpawner.updateBlockState();
                        abstractSpawner.clearShulkerDisplay();
                        if (clearData) {
                            SpawnerData.removeData(spawner);
                        }
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
            BlockState block = location.getBlock().getState();
            Optional<MetadataValue> optionalMetadataValue = block.getMetadata(DEFAULT_META_KEY).stream().findAny();
            if (!optionalMetadataValue.isPresent()) {
                return Optional.empty();
            }
            Object rawObj = optionalMetadataValue.get().value();
            if (!(rawObj instanceof ItemStack)) {
                return Optional.empty();
            }
            ItemStack itemStack = (ItemStack) rawObj;
            NBTItem nbtItem = new NBTItem(itemStack);
            String rawClass = nbtItem.getString("class");
            String rawWrapper = nbtItem.getString("wrapperClass");
            if (rawClass == null) {
                return Optional.empty();
            }
            Class<?> rawSpawnerClass;
            Class<?> rawWrapperClass;
            Class<? extends AbstractSpawner> spawnerClass;
            try {
                rawSpawnerClass = Class.forName(rawClass);
                if (!AbstractSpawner.class.isAssignableFrom(rawSpawnerClass)) {
                    return Optional.empty();
                }
                spawnerClass = rawSpawnerClass.asSubclass(AbstractSpawner.class);
                if (rawWrapper != null) {
                    rawWrapperClass = Class.forName(rawWrapper);
                    if (!CustomAreaSpawner.class.isAssignableFrom(rawWrapperClass)) {
                        return Optional.empty();
                    }
                    Class<? extends CustomAreaSpawner> wrapperClass = rawWrapperClass.asSubclass(CustomAreaSpawner.class);
                    Spawner.ItemWrapper<CustomAreaSpawner> specificWrapper = (Spawner.ItemWrapper<CustomAreaSpawner>) wrapperClass.getMethod("getSpecificWrapper", Class.class).invoke(null, spawnerClass);
                    Optional<OfflineSpawner<CustomAreaSpawner>> optionalOfflineSpawner = specificWrapper.fromItem(itemStack);
                    if (!optionalOfflineSpawner.isPresent()) {
                        return Optional.empty();
                    }
                    OfflineSpawner<CustomAreaSpawner> offlineSpawner = optionalOfflineSpawner.get();
                    Optional<CustomAreaSpawner> optionalCustomAreaSpawner = specificWrapper.toLiveAtLocation(offlineSpawner, location);
                    if (!optionalOfflineSpawner.isPresent()) {
                        return Optional.empty();
                    }
                    return Optional.of(optionalCustomAreaSpawner.get());
                } else {
                    Spawner.ItemWrapper wrapper = (Spawner.ItemWrapper) spawnerClass.getMethod("getWrapper").invoke(null);
                    Optional<OfflineSpawner> offlineSpawner = wrapper.fromItem(itemStack);
                    if (!offlineSpawner.isPresent()) {
                        return Optional.empty();
                    }
                    return wrapper.toLiveAtLocation(offlineSpawner.get(), location);
                }
            } catch (ReflectiveOperationException | ClassCastException ex) {
                return Optional.empty();
            }
        }
    }

}
