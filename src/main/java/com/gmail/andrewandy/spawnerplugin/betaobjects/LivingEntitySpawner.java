package com.gmail.andrewandy.spawnerplugin.betaobjects;

import com.gmail.andrewandy.spawnerplugin.util.Common;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class LivingEntitySpawner extends Spawner implements StackableSpawner {

    private final EntityType spawnedType;
    private final int maxSize;
    private final List<OfflineSpawner> stacked;

    public LivingEntitySpawner(ItemStack itemStack, Location location) throws IllegalAccessException {
        super(itemStack, location);
        Optional<LivingEntitySpawner> spawner = getFromItem(itemStack, location);
        if (!spawner.isPresent()) {
            throw new IllegalAccessException();
        }
        LivingEntitySpawner cloned = spawner.get();
        this.maxSize = cloned.maxSize;
        this.spawnedType = cloned.spawnedType;
        this.stacked = cloned.stacked;
    }

    public LivingEntitySpawner(EntityType entityType, int delay, int maxSize, Location location) {
        super(delay, location);
        this.spawnedType = Objects.requireNonNull(entityType);
        this.maxSize = maxSize;
        this.stacked = new ArrayList<>(maxSize);
        Objects.requireNonNull(Objects.requireNonNull(location).getWorld());
    }

    @Override
    public int getSize() {
        return stacked.size();
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public void add(OfflineSpawner spawner) {
        if (isFull()) {
            return;
        }
        stacked.add(spawner);
    }

    @Override
    public void addAll(Collection<OfflineSpawner> spawners) {
        if (spawners.size() + getSize() > maxSize) {
            throw new IllegalArgumentException("Unable to exceed max stack size!");
        }
        stacked.addAll(spawners);
    }

    @Override
    public void withdraw(OfflineSpawner spawner) {
        stacked.remove(spawner);
    }

    @Override
    public void clear() {
        stacked.clear();
    }

    @Override
    public boolean canStack(OfflineSpawner spawner) {
        boolean target = Objects.requireNonNull(spawner).getOriginalClass().isAssignableFrom(LivingEntitySpawner.class);
        return target && spawner.getOwner().equals(super.getOwner()) || super.getTeamMembers().contains(spawner.getOwner());
    }

    @Override
    public List<OfflineSpawner> getStacked() {
        return new ArrayList<>(stacked);
    }


    public EntityType getSpawnedType() {
        return spawnedType;
    }

    @Override
    public Spawner clone() {
        return new LivingEntitySpawner(spawnedType, getDelay(), maxSize, getLocation());
    }

    public ItemStack getAsItem() {
        ItemStack item = new ItemStack(Material.SPAWNER);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(Common.colourise("&e" + Common.capitalise(spawnedType.name().toLowerCase()) + "&e Spawner"));
        itemMeta.setLore(Arrays.asList(
                Common.colourise(""),
                Common.colourise("&b&lInformation:"),
                Common.colourise("  &7-&a Mob: " + Common.capitalise(spawnedType.name().toLowerCase())),
                Common.colourise("  &7-&e Size: " + getSize()),
                Common.colourise("  &7-&c Max Size: " + maxSize)
        ));
        item.setItemMeta(itemMeta);
        return getAsItem(item);
    }

    public ItemStack getAsItem(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack.clone());
        nbtItem.setString("owner", super.getOwner().toString());
        nbtItem.setString("spawner", "mob");
        nbtItem.setString("entityType", spawnedType.name());
        nbtItem.setInteger("delay", super.getDelay());
        nbtItem.setObject("stacked", stacked);
        nbtItem.setObject("teamMembers", super.getTeamMembers());
        nbtItem.setInteger("maxSize", maxSize);
        return nbtItem.getItem();
    }

    @Override
    protected Optional<LivingEntitySpawner> getFromItem(ItemStack item, Location location) {
        Optional<LivingEntitySpawner> optional = Optional.empty();
        if (!instanceOfSpawner(item)) {
            return optional;
        }
        NBTItem nbtItem = new NBTItem(item.clone());
        try {
            int delay = nbtItem.getInteger("delay");
            //Should be fine.
            @SuppressWarnings("Unchecked")
            List<OfflineSpawner> stacked = (List<OfflineSpawner>) nbtItem.getObject("stacked", List.class);
            List<UUID> teamMembers = (List<UUID>) nbtItem.getObject("teamMembers", List.class);
            int maxSize = nbtItem.getInteger("maxSize");
            EntityType entityType = EntityType.valueOf(nbtItem.getString("entityType"));
            UUID owner = UUID.fromString(nbtItem.getString("owner"));
            LivingEntitySpawner spawner = new LivingEntitySpawner(entityType, delay, maxSize, location);
            spawner.setOwner(owner);
            spawner.addAll(stacked);
            teamMembers.forEach(spawner::addTeamMember);
            optional = Optional.of(spawner);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ignored) {
        }
        return optional;
    }


    @Override
    public boolean instanceOfSpawner(ItemStack itemStack) {
        NBTItem item = new NBTItem(itemStack);
        if (!item.hasNBTData()) {
            return false;
        }
        String type = item.getString("spawner");
        return type != null && type.equalsIgnoreCase("mob");
    }

}
