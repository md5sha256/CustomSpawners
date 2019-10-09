package com.gmail.andrewandy.spawnerplugin.object;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import java.util.*;

public abstract class Spawner {

    private BlockState block;
    private UUID owner;
    private Set<UUID> trusted;

    protected Spawner(Block fromBlock) throws IllegalAccessException {
        Objects.requireNonNull(fromBlock);
    }

    protected void setup(BlockState block, UUID owner, Set<UUID> trusted) {
        this.block = Objects.requireNonNull(block);
        this.owner = Objects.requireNonNull(owner);
        this.trusted = Objects.requireNonNull(trusted);
    }

    abstract ItemStack toItemStack();

    protected NBTItem getBase() {
        NBTItem nbtItem = new NBTItem(new ItemStack(block.getType()));
        nbtItem.setString("customSpawner", this.getClass().getName());
        nbtItem.setString("owner", owner.toString());
        nbtItem.setObject("trusted", trusted);
        nbtItem.setString("blockData", block.getBlockData().getAsString());
        return nbtItem;
    }

    public UUID getOwner() {
        return owner;
    }

    public BlockState getBlock() {
        return block;
    }

    public Set<UUID> getTrusted() {
        return trusted;
    }

    public static Spawner fromBlock(Block block) throws IllegalAccessException{
        Objects.requireNonNull(block);
        if (!block.hasMetadata("customSpawner")) {
            throw new IllegalAccessException("Invalid block.");
        }
        List<MetadataValue> meta = block.getMetadata("customSpawner");
        assert !meta.isEmpty();
        try {
            Class<?> raw = Class.forName(meta.get(0).asString());
            Class<? extends Spawner> clazz = raw.asSubclass(Spawner.class);
            return clazz.getDeclaredConstructor(Block.class).newInstance(block);
        } catch (ReflectiveOperationException ex) {
            IllegalStateException e = new IllegalStateException();
            e.addSuppressed(ex);
            throw e;
        }
    }

}
