package com.gmail.andrewandy.spawnerplugin.betaobjects;

import com.gmail.andrewandy.spawnerplugin.util.Common;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public abstract class Spawner implements Cloneable {

    protected BlockDataMeta blockMeta;
    private int delay;
    private Location location;
    private UUID owner;
    private List<UUID> team;

    public Spawner(BlockDataMeta meta) {

    }

    /**
     * This method should only be used to create a spawner object from a serialized ItemStack.
     *
     * @param serialized The serialized ItemStack.
     * @param location   The location of the spawner.
     * @throws IllegalAccessException Thrown if the serialized object is invalid, or if there were any errors in deserialization.
     */
    public Spawner(ItemStack serialized, Location location) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    public Spawner(int delay, Location location) {
        this.location = Objects.requireNonNull(location);
        Objects.requireNonNull(location.getWorld());
        this.delay = delay;
    }

    private Spawner() {
    }

    public static String getTestIdentifier(Location location) {
        Objects.requireNonNull(location);
        Objects.requireNonNull(location.getWorld());
        return location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public List<UUID> getTeamMembers() {
        return team;
    }

    public void setTeamMembers(List<UUID> team) {
        this.team = team;
    }

    public void addTeamMember(UUID team) {
        if (team.equals(owner)) {
            return;
        }
        this.team.add(Objects.requireNonNull(team));
    }


    public ItemStack getAsItem() {
        return getAsItem("&eCustom Spawner");
    }

    public ItemStack getAsItem(String name) {
        ItemStack template = new ItemStack(Material.SPAWNER);
        ItemMeta meta = template.getItemMeta();
        meta.setDisplayName(Common.colourise(name));
        template.setItemMeta(meta);
        return getAsItem(template);
    }

    abstract ItemStack getAsItem(ItemStack base);

    abstract Optional<? extends Spawner> getFromItem(ItemStack item, Location location);

    abstract BlockDataMeta getAsMeta(BlockDataMeta meta)

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

    public abstract Spawner clone();

}
