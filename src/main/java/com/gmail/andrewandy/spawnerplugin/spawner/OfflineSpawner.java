package com.gmail.andrewandy.spawnerplugin.spawner;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class OfflineSpawner<T extends Spawner> {

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
}
