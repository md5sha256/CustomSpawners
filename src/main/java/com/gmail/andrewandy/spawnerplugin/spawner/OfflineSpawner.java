package com.gmail.andrewandy.spawnerplugin.spawner;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public final class OfflineSpawner<T extends Spawner> {

    private Class<T> clazz;
    private ItemStack itemStack;

    public OfflineSpawner(Class<T> clazz, ItemStack itemStack) {
        this.clazz = clazz;
        this.itemStack = Objects.requireNonNull(itemStack).clone();
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
