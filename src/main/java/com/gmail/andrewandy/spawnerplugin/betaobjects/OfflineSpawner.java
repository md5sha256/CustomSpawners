package com.gmail.andrewandy.spawnerplugin.betaobjects;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OfflineSpawner {

    private final UUID owner;
    private final UUID spawnerID;
    private final List<UUID> team;
    private final Class<? extends Spawner> originalClass;
    private NBTItem asItemStack;

    public OfflineSpawner(Spawner spawner) {
        this(spawner, UUID.randomUUID());
    }

    public OfflineSpawner(Spawner spawner, UUID spawnerID) {
        this.owner = spawner.getOwner();
        this.team = spawner.getTeamMembers();
        this.spawnerID = spawnerID;
        this.originalClass = spawner.getClass();
        asItemStack = toItemStack(spawner);
    }

    public static String formatLocation(Location location) {
        if (location == null) {
            return "null";
        }
        return location.getWorld() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockX() + ";";
    }

    public static Optional<OfflineSpawner> asOfflineSpawner(ItemStack itemStack, Location location) {
        NBTItem item = new NBTItem(itemStack.clone());
        String clazz = item.getString("originalClass");
        OfflineSpawner target;
        if (clazz == null) {
            return Optional.empty();
        }
        try {
            Class<?> raw = Class.forName(clazz);
            Class<? extends Spawner> casted = raw.asSubclass(Spawner.class);
            Spawner s = casted.getDeclaredConstructor(ItemStack.class, Location.class).newInstance(itemStack, location);
            target = new OfflineSpawner(s);
        } catch (ReflectiveOperationException ignored) {
            return Optional.empty();
        }
        return Optional.of(target);

    }

    public Class<? extends Spawner> getOriginalClass() {
        return originalClass;
    }

    public List<UUID> getTeamMembers() {
        return new LinkedList<>(team);
    }

    public UUID getOwner() {
        return owner;
    }

    public UUID getSpawnerID() {
        return spawnerID;
    }

    public String getSpawnerIDWithLocation() {
        return asItemStack.getString("originalLocation");
    }

    private NBTItem toItemStack(Spawner spawner) {
        NBTItem item = new NBTItem(spawner.getAsItem());
        item.setString("originalClass", originalClass.getName());
        item.setString("ownerID", owner.toString());
        item.setString("spawnerID", spawnerID.toString());
        item.setObject("teamMembers", team);
        item.setString("originalLocation", formatLocation(spawner.getLocation()));
        return item;
    }

    public ItemStack getAsItemStack() {
        return asItemStack.getItem();
    }

    public Optional<Spawner> asSpawner(Location location) {
        try {
            return Optional.of(originalClass.getDeclaredConstructor(ItemStack.class, Location.class).newInstance(getAsItemStack(), location));
        } catch (ReflectiveOperationException ex) {
            return Optional.empty();
        }
    }
}
