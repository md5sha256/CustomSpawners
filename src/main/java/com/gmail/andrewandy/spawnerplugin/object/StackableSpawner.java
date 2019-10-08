package com.gmail.andrewandy.spawnerplugin.object;

import com.gmail.andrewandy.spawnerplugin.util.Common;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class StackableSpawner extends Spawner {

    private EntityType spawnedType;
    private String identifier;
    private int delay;
    private Location location;
    private int currentSize = 1;
    private int maxSize;

    public StackableSpawner(EntityType entityType, int delay, int maxSize, Location location) {
        this.spawnedType = Objects.requireNonNull(entityType);
        this.delay = delay;
        this.maxSize = maxSize;
        Objects.requireNonNull(Objects.requireNonNull(location).getWorld());
        this.location = location.clone();
        this.identifier = location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
    }

    public StackableSpawner(EntityType entityType, int delay, int currentSize, int maxSize, Location location) {
        this(entityType, delay, maxSize, location);
        this.currentSize = currentSize;
    }

    public static boolean isSpawner(ItemStack itemStack) {
        NBTItem item = new NBTItem(itemStack);
        if (!item.hasNBTData()) {
            return false;
        }
        String s = item.getString("spawner");
        return s != null && s.equalsIgnoreCase("true");
    }

    public static Optional<StackableSpawner> fromItemStack(ItemStack itemStack, Location location) {
        Optional<StackableSpawner> target = Optional.empty();
        if (!isSpawner(itemStack)) {
            return target;
        }
        NBTItem item = new NBTItem(itemStack);
        try {
            EntityType entityType = EntityType.valueOf(item.getString("entityType"));
            int delay = item.getInteger("delay");
            int size = item.getInteger("size");
            int maxSize = item.getInteger("maxSize");
            target = Optional.of(new StackableSpawner(entityType, delay, size, maxSize, location));
        } catch (IllegalArgumentException ignored) {
        }
        return target;
    }

    public boolean canStack(Spawner spawner) {
        return Objects.requireNonNull(spawner).getSpawnedType() == spawnedType && currentSize < maxSize;
    }

    public void stack(Spawner spawner) {
        if (!canStack(spawner)) {
            throw new IllegalArgumentException("Invalid Spawner.");
        }
        this.maxSize++;
    }

    public StackableSpawner withdrawFromStack() {
        this.currentSize = -1;
        return new StackableSpawner(spawnedType, delay, location);
    }

    public void breakStack() {
        this.currentSize = 0;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Spawner clone() {
        return null;
    }

    public ItemStack getAsItem() {
        ItemStack item = new ItemStack(Material.SPAWNER);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(Common.colourise("&e" + Common.capitalise(spawnedType.name().toLowerCase()) + "&e Spawner"));
        itemMeta.setLore(Arrays.asList(
                Common.colourise(""),
                Common.colourise("&b&lInformation:"),
                Common.colourise("  &7-&a Mob: " + Common.capitalise(spawnedType.name().toLowerCase())),
                Common.colourise("  &7-&e Size: " + currentSize),
                Common.colourise("  &7-&c Max Size: " + maxSize)
        ));
        item.setItemMeta(itemMeta);
        return getAsItem(item);
    }

    public ItemStack getAsItem(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setString("spawner", "true");
        nbtItem.setString("entityType", spawnedType.name());
        nbtItem.setInteger("delay", delay);
        nbtItem.setInteger("size", currentSize);
        nbtItem.setInteger("maxSize", maxSize);
        return nbtItem.getItem();
    }

    @Override
    protected Optional<StackableSpawner> getFromItem(ItemStack item, Location location) {
        Optional<StackableSpawner> optional = Optional.empty();
        if (!instanceOfSpawner(item)) {
            return optional;
        }
        NBTItem nbtItem = new NBTItem(target.clone());
        try {
            int delay = nbtItem.getInteger("delay");
            int amount = nbtItem.getInteger("amount");
            boolean glowing = nbtItem.getBoolean("glowing");
            NBTItem spawned = nbtItem.getObject("item", NBTItem.class);
            optional = Optional.of(new StackableSpawner(spawned, delay, amount, location, glowing));
        } catch (IllegalArgumentException ignored) {
        }
        return optional;
    }

    @Override
    public int getDelay() {
        return delay;
    }

    @Override
    public boolean instanceOfSpawner(ItemStack itemStack) {
        return false;
    }
}
