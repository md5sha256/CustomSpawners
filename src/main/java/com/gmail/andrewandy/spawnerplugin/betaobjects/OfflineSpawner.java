package com.gmail.andrewandy.spawnerplugin.betaobjects;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.*;

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

    private OfflineSpawner(UUID owner, UUID spawnerID, List<UUID> team, Class<? extends Spawner> originalClass) {
        this.owner = Objects.requireNonNull(owner);
        this.spawnerID = Objects.requireNonNull(spawnerID);
        this.team = Objects.requireNonNull(team);
        this.originalClass = originalClass;
    }

    public static Optional<Location> fromFormatted(String formatted) {
        try {
            String[] split = formatted.split(";");
            World world = Bukkit.getServer().getWorld(split[0]);
            int x = Integer.parseInt(split[1]);
            int y = Integer.parseInt(split[2]);
            int z = Integer.parseInt(split[3]);
            return Optional.of(new Location(world, x, y, z));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public static String formatLocation(Location location) {
        if (location == null) {
            return "null";
        }
        return location.getWorld() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockX() + ";";
    }

    public static Optional<OfflineSpawner> fromItemStack(ItemStack itemStack) {
        NBTItem item = new NBTItem(itemStack);
        String raw = item.getString("originalClass");
        Class<? extends Spawner> clazz;
        UUID owner;
        UUID spawnerID;
        List<UUID> team;
        try {
            Class<?> rawClass = Class.forName(raw);
            if (!rawClass.isAssignableFrom(Spawner.class)) {
                return Optional.empty();
            }
            clazz = rawClass.asSubclass(Spawner.class);
            owner = UUID.fromString(item.getString("ownerID"));
            spawnerID = UUID.fromString(item.getString("spawnerID"));
            List rawList = item.getObject("teamMembers", List.class);
            team = (List<UUID>) rawList;
        } catch (ClassNotFoundException | ClassCastException | IllegalArgumentException ex) {
            return Optional.empty();
        }
        return Optional.of(new OfflineSpawner(owner, spawnerID, team, clazz));
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
