package com.gmail.andrewandy.spawnerplugin.spawner;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;

public final class OfflineSpawner<T extends Spawner> {

    private Class<T> clazz;
    private ItemStack itemStack;

    public OfflineSpawner(Class<T> clazz, ItemStack itemStack) {
        this.clazz = clazz;
        this.itemStack = Objects.requireNonNull(itemStack).clone();
    }

    public static Optional<OfflineSpawner<?>> ofItem(ItemStack itemStack) {
        NBTItem item = new NBTItem(itemStack);
        String rawClass = item.getString("class");
        if (rawClass == null) {
            return Optional.empty();
        }
        try {
            Class<?> clazz = Class.forName(rawClass);
            if (!clazz.isAssignableFrom(Spawner.class)) {
                return Optional.empty();
            }
            Class<? extends Spawner> spawnerClass = clazz.asSubclass(Spawner.class);
            return Optional.of(new OfflineSpawner<>(spawnerClass, itemStack));
        } catch (ClassNotFoundException ex) {
            return Optional.empty();
        }
    }

    public static Optional<Spawner.ItemWrapper<?>> getWrapperOf(ItemStack itemStack) {
        if (ofItem(itemStack).isPresent()) {
            assert Spawner.class.isAssignableFrom(ofItem(itemStack).get().getOriginalClass());
            Class<? extends Spawner> clazz = ofItem(itemStack).get().getOriginalClass();
            try {
                return Optional.of((Spawner.ItemWrapper<?>) clazz.getMethod("getWrapper").invoke(null));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            }
        }
        return Optional.empty();
    }

    public Optional<Spawner.ItemWrapper<?>> getWrapper() {
        return getWrapperOf(this.itemStack);
    }

    public Class<T> getOriginalClass() {
        return clazz;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public boolean isValidSpawner() {
        return T.getWrapper().isSpawner(itemStack);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OfflineSpawner)) {
            return false;
        }
        OfflineSpawner<?> offlineSpawner = (OfflineSpawner<?>) o;
        //This or something like this.hashCode() == offlineSpawner.hashCode().
        return offlineSpawner.itemStack.isSimilar(this.itemStack) && offlineSpawner.getOriginalClass().equals(clazz);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = hash * clazz.getCanonicalName().hashCode();
        hash = hash * itemStack.hashCode();
        return hash;
    }
}
