package com.gmail.andrewandy.spawnerplugin.object;

import com.gmail.andrewandy.spawnerplugin.util.Common;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class LivingEntitySpawner extends Spawner implements Stackable<LivingEntitySpawner> {

    private static final Type TYPE = new TypeToken<Collection<OfflineSpawner>>() {
    }.getType();
    private final Collection<OfflineSpawner> stacked;
    private final int maxSize;
    private String name;
    private List<String> customLore;

    protected LivingEntitySpawner(Block fromBlock) throws IllegalAccessException {
        super(fromBlock);
        BlockState state = fromBlock.getState(true);
        if (!state.hasMetadata("customSpawner")) {
            throw new IllegalAccessException();
        }
        List<MetadataValue> values = state.getMetadata("customSpawner");
        assert !values.isEmpty();
        try {
            Class<?> raw = Class.forName(values.get(0).asString());
            if (raw != this.getClass() && !raw.isAssignableFrom(this.getClass())) {
                throw new IllegalAccessException();
            }
            values = state.getMetadata("stacked");
            assert !values.isEmpty();
            String obj = values.get(0).asString();
            stacked = new GsonBuilder().create().fromJson(obj, TYPE);
            this.maxSize = state.getMetadata("maxSize").get(0).asInt();
        } catch (ClassNotFoundException ex) {
            IllegalAccessException e = new IllegalAccessException();
            e.addSuppressed(ex);
            throw e;
        }
    }

    public String getCustomName() {
        return name;
    }

    public void setCustomName(String name) {
        this.name = Common.colourise(name);
    }

    public List<String> getCustomLore() {
        return customLore;
    }

    public void setCustomLore(List<String> customLore) {
        this.customLore = customLore;
    }

    @Override
    ItemStack toItemStack() {
        ItemStack stack = getBase().getItem();
        ItemMeta meta = stack.getItemMeta();
        if (name != null) {
            meta.setDisplayName(name);
        }
        if (customLore != null) {
            meta.setLore(customLore);
        }
        stack.setItemMeta(meta);
        NBTItem item = new NBTItem(stack);
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(stacked, TYPE);
        item.setString("stacked", json);
        return item.getItem();
    }

    @Override
    public Collection<OfflineSpawner> getStacked() {
        return new LinkedList<>(stacked);
    }

    @Override
    public void stack(OfflineSpawner offlineSpawner) {

    }

    @Override
    public boolean stackEligible(Object o) {
        if (o instanceof LivingEntitySpawner) {
            LivingEntitySpawner spawner = (LivingEntitySpawner) o;
            if (spawner.getSize() + getSize() > getMaxSize()) {
                return false;
            }
            return spawner.getOwner().equals(getOwner()) || getTrusted().contains(spawner.getOwner());
        } else if (o instanceof OfflineSpawner) {
            OfflineSpawner offlineSpawner = (OfflineSpawner) o;
            if (!offlineSpawner.getOriginalClass().equals(this.getClass())) {
                return false;
            }
            return offlineSpawner.getOwner().equals(getOwner()) || getTrusted().contains(offlineSpawner.getOwner());
        } else {
            return false;
        }
    }

    @Override
    public void withdraw(OfflineSpawner offlineSpawner) {

    }

    @Override
    public void stack(LivingEntitySpawner stackable) {

    }

    @Override
    public void clearStack() {

    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

}
