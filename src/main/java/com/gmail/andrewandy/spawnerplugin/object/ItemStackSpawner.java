package com.gmail.andrewandy.spawnerplugin.object;

import com.gmail.andrewandy.spawnerplugin.util.Common;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class ItemStackSpawner extends Spawner implements ItemSpawner {

    private NBTItem base;
    private int amount;
    private boolean glowing;

    public ItemStackSpawner(NBTItem base, int delay, Location location, boolean glowing) {
        this(base, delay, 1, location, glowing);
    }

    public ItemStackSpawner(NBTItem base, int delay, int amount, Location location, boolean glowing) {
        super(delay, location);
        this.base = Objects.requireNonNull(base);
        this.glowing = glowing;
        this.amount = amount;
    }

    public ItemStackSpawner(ItemStack serialized, Location location) throws IllegalAccessException {
        super(serialized, location);
    }

    public boolean isGlowing() {
        return glowing;
    }

    @Override
    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }

    @Override
    public NBTItem getBase() {
        return base;
    }

    @Override
    public ItemStackSpawner clone() {
        return new ItemStackSpawner(base, super.getDelay(), amount, super.getLocation(), glowing);
    }

    @Override
    public ItemStack getAsItem() {
        ItemStack item = new ItemStack(base.getItem().getType());
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(Arrays.asList(
                Common.colourise(""),
                Common.colourise("&b&lInformation:"),
                Common.colourise("  &7-&a Item: " + Common.colourise(itemMeta.getDisplayName())),
                Common.colourise("  &7-&c Amount: " + amount)
        ));
        item.setItemMeta(itemMeta);
        return getAsItem(item);
    }

    @Override
    public ItemStack getAsItem(ItemStack base) {
        NBTItem nbtItem = new NBTItem(base);
        nbtItem.setString("spawner", "itemFactory");
        nbtItem.setInteger("delay", super.getDelay());
        nbtItem.setInteger("amount", amount);
        nbtItem.setBoolean("isGlowing", glowing);
        nbtItem.setObject("item", this.base);
        return nbtItem.getItem();
    }


    @Override
    protected Optional<ItemStackSpawner> getFromItem(ItemStack target, Location location) {
        Optional<ItemStackSpawner> optional = Optional.empty();
        if (!instanceOfSpawner(target)) {
            return optional;
        }
        NBTItem item = new NBTItem(target.clone());
        try {
            int delay = item.getInteger("delay");
            int amount = item.getInteger("amount");
            boolean glowing = item.getBoolean("glowing");
            NBTItem spawned = item.getObject("item", NBTItem.class);
            optional = Optional.of(new ItemStackSpawner(spawned, delay, amount, location, glowing));
        } catch (IllegalArgumentException ignored) {
        }
        return optional;
    }

    @Override
    public boolean instanceOfSpawner(ItemStack itemStack) {
        NBTItem item = new NBTItem(itemStack);
        String type = item.getString("spawner");
        return type != null && type.equalsIgnoreCase("itemFactory");
    }
}
