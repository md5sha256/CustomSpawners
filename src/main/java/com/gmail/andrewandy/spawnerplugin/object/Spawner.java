package com.gmail.andrewandy.spawnerplugin.object;

import com.gmail.andrewandy.spawnerplugin.util.Common;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;
import java.util.Optional;

public abstract class Spawner implements Cloneable {

    abstract ItemStack getAsItem();
    private int delay;
    private Location location;

    /**
     * This method should only be used to create a spawner object from a serialized ItemStack.
     * @param serialized The serialized ItemStack.
     * @param location The location of the spawner.
     * @throws IllegalAccessException Thrown if the serialized object is invalid, or if there were any errors in deserialization.
     */
    public Spawner(ItemStack serialized, Location location) throws IllegalAccessException {
        if (!instanceOfSpawner(Objects.requireNonNull(serialized))) {
            throw new IllegalAccessException();
        }
        Optional<? extends Spawner> spawner = getFromItem(serialized, location);
        if (!spawner.isPresent()) {
            throw new IllegalAccessException("Invalid spawner!");
        }
        Spawner target = spawner.get();
        this.delay = target.delay;
        this.location = location.clone();
    }

    public Spawner(int delay, Location location) {
        this.location = Objects.requireNonNull(location);
        Objects.requireNonNull(location.getWorld());
        this.delay = delay;
    }

    private Spawner() {}

    public ItemStack getAsItem(String name) {
        ItemStack template = new ItemStack(Material.SPAWNER);
        ItemMeta meta = template.getItemMeta();
        meta.setDisplayName(Common.colourise(name));
        template.setItemMeta(meta);
        return getAsItem(template);
    }

    abstract ItemStack getAsItem(ItemStack base);

    protected abstract Optional<? extends Spawner> getFromItem(ItemStack item, Location location);

    public int getDelay() {
        return delay;
    }

    public abstract boolean instanceOfSpawner(ItemStack itemStack);

    public Location getLocation() {
        return location.clone();
    }

    public String getIdentifier() {
        Location location = getLocation();
        return location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
    }

    public static String getTestIdentifier(Location location) {
        Objects.requireNonNull(location);
        Objects.requireNonNull(location.getWorld());
        return location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
    }

    public abstract Spawner clone();

}
