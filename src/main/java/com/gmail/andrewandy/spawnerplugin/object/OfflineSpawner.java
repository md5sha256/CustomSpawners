package com.gmail.andrewandy.spawnerplugin.object;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public final class OfflineSpawner extends ItemStack{

    private final Class<? extends Spawner> originalClass;
    private final UUID owner;

    public OfflineSpawner(Spawner spawner) {
        this.originalClass = spawner.getClass();
        this.owner = spawner.getOwner();
        ItemStack stack = spawner.toItemStack();
        super.setData(stack.getData());
        super.setAmount(stack.getAmount());
        super.setItemMeta(super.getItemMeta());
    }

    public Class<? extends Spawner> getOriginalClass() {
        return originalClass;
    }

    public UUID getOwner() {
        return owner;
    }

    public ItemStack getSerializable() {
        NBTItem nbtItem = new NBTItem(this);
        nbtItem.setString("originalClass", originalClass.getName());
        return nbtItem.getItem();
    }

}
