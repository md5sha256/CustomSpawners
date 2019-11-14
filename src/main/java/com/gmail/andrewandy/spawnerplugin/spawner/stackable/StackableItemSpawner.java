package com.gmail.andrewandy.spawnerplugin.spawner.stackable;

import com.gmail.andrewandy.corelib.util.gui.Gui;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.spawner.AbstractSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.ItemSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.OfflineSpawner;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.lang.reflect.Type;
import java.util.*;

public class StackableItemSpawner extends AbstractSpawner implements StackableSpawner<StackableItemSpawner> {

    private final static int VERSION = 0;
    private final static ItemWrapper<StackableItemSpawner> WRAPPER = new WrapperImpl();
    private final int maxSize;
    private Collection<OfflineSpawner<StackableItemSpawner>> stacked = new HashSet<>();

    private ItemStack toSpawn;

    public StackableItemSpawner(Location location, Material material, UUID owner, int tickDelay, float spawnChance, ItemStack toSpawn, int maxSize) {
        super(location, material, owner, tickDelay, spawnChance);
        this.toSpawn = Objects.requireNonNull(toSpawn).clone();
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
    }

    public StackableItemSpawner(Location location, Material material, UUID owner, int tickDelay, ItemStack toSpawn, int maxSize) {
        this(location, material, owner, tickDelay, toSpawn, null, maxSize);
    }

    public StackableItemSpawner(Location location, Material material, UUID owner, int tickDelay, ItemStack toSpawn, Collection<UUID> peers, int maxSize) {
        super(location, material, owner, tickDelay, peers);
        this.toSpawn = Objects.requireNonNull(toSpawn).clone();
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
    }

    private static Optional<ItemStack> convertFromOldVersion(ItemStack itemStack) {
        //Since this is version 0, there is no old version.
        return Optional.of(itemStack);
    }

    public static ItemWrapper<? extends StackableItemSpawner> getWrapper() {
        return WRAPPER;
    }

    @Override
    public BlockState getAsBlockState() {
        BlockState blockState = getLocation().getBlock().getState(true);
        MetadataValue value = new FixedMetadataValue(SpawnerPlugin.getInstance(), WRAPPER.toItem(this));
        blockState.setMetadata("itemSpawnerData", value);
        return blockState;
    }

    @Override
    public Optional<Gui> getDisplayUI() {
        return Optional.empty();
    }

    @Override
    public void initialize() {
        //Nothing needs to be initialized.
    }

    @Override
    protected void tick() {
        super.tick();
        Block block = getLocation().getBlock();
        Block toSpawn = null;
        for (BlockFace blockFace : BlockFace.values()) {
            Block b = getLocation().getBlock().getRelative(blockFace);
            if (block.getType().isAir()) {
                continue;
            }
            toSpawn = b;
            break;
        }
        if (toSpawn == null) {
            //Check was done in super.
            return;
        }
        if (super.shouldSpawn()) {
            toSpawn.getWorld().dropItemNaturally(toSpawn.getLocation(), this.toSpawn.clone());
        }
    }

    public ItemStack getSpawnedItem() {
        return toSpawn.clone();
    }

    @Override
    public Collection<OfflineSpawner<StackableItemSpawner>> getStacked() {
        return Collections.unmodifiableCollection(stacked);
    }

    @Override
    public void stack(OfflineSpawner<StackableItemSpawner> spawner) {
        if (isFull()) {
            return;
        }
        stacked.add(spawner);
    }

    @Override
    public void remove(OfflineSpawner<StackableItemSpawner> spawner) {
        stacked.remove(spawner);
    }

    @Override
    public int maxSize() {
        return maxSize;
    }

    @Override
    public boolean stackAll(Collection<OfflineSpawner<StackableItemSpawner>> collection) {
        if (!canStack(Objects.requireNonNull(collection))) {
            return false;
        }
        return stacked.addAll(collection);
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = hash * super.hashCode();
        hash = hash * maxSize;
        hash = hash * stacked.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof StackableItemSpawner)) {
            return false;
        }
        StackableItemSpawner target = (StackableItemSpawner) o;
        return target.hashCode() == this.hashCode();
    }

    private static class WrapperImpl extends ItemWrapper<StackableItemSpawner> {

        WrapperImpl() {
        }

        @Override
        public ItemStack toItem(StackableItemSpawner spawner) {
            if (spawner == null) {
                throw new IllegalArgumentException("Spawner cannot be null.");
            }
            ItemStack itemStack = new ItemStack(Material.SPAWNER);
            NBTItem nbtItem = new NBTItem(itemStack);
            Gson gson = new GsonBuilder().create();
            nbtItem.setString("class", StackableItemSpawner.class.getName());
            nbtItem.setInteger("classVersion", StackableItemSpawner.VERSION);
            nbtItem.setInteger("delay", spawner.delay);
            nbtItem.setInteger("maxSize", spawner.maxSize);
            nbtItem.setFloat("spawnChance", spawner.getSpawnChance());
            nbtItem.setString("owner", spawner.owner.toString());
            nbtItem.setObject("spawnedItem", spawner.toSpawn);
            Type type = new TypeToken<Collection<UUID>>() {
            }.getType();
            String json = gson.toJson(spawner.peers, type);
            nbtItem.setString("peers", json);
            return nbtItem.getItem();
        }

        @Override
        public Optional<StackableItemSpawner> place(OfflineSpawner<StackableItemSpawner> spawner, Location location) {
            ItemStack original = spawner.getItemStack().clone();
            NBTItem nbtItem = new NBTItem(original);
            Objects.requireNonNull(Objects.requireNonNull(location).getWorld());
            String rawClass = nbtItem.getString("class");
            if (rawClass == null) {
                return Optional.empty();
            }
            Class<?> raw;
            try {
                raw = Class.forName(rawClass);
                if (!raw.isAssignableFrom(ItemSpawner.class)) {
                    return Optional.empty();
                }
            } catch (ClassNotFoundException ex) {
                return Optional.empty();
            }
            //Check validity
            float spawnChance;
            int delay;
            int maxSize;
            UUID owner;
            int version;
            Material material;
            Collection<UUID> peers;
            delay = nbtItem.getInteger("delay");
            spawnChance = nbtItem.getFloat("spawnChance");
            maxSize = nbtItem.getInteger("maxSize");
            String rawMaterial = nbtItem.getString("material");
            if (rawMaterial == null) {
                return Optional.empty();
            }
            material = Material.valueOf(rawMaterial);
            String rawUUID = nbtItem.getString("owner");
            Gson gson = new GsonBuilder().create();
            String rawPeers = nbtItem.getString("peers");
            version = nbtItem.getInteger("classVersion");
            if (rawPeers == null || rawUUID == null) {
                return Optional.empty();
            }
            if (version != VERSION) {
                Optional<ItemStack> optional = convertFromOldVersion(spawner.getItemStack());
                if (optional.isPresent()) {
                    Optional<OfflineSpawner<StackableItemSpawner>> offlineSpawner = fromItem(optional.get());
                    if (!offlineSpawner.isPresent()) {
                        throw new IllegalStateException("Unable to convert itemstack.");
                    }
                    return place(offlineSpawner.get(), location);
                } else {
                    return Optional.empty();
                }
            }
            try {
                //Check validity
                owner = UUID.fromString(rawUUID);
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
            Type type = new TypeToken<Collection<UUID>>() {
            }.getType();
            peers = gson.fromJson(rawPeers, type);
            assert peers != null;
            ItemStack toSpawn = nbtItem.getObject("spawnedItem", ItemStack.class);
            if (toSpawn == null) {
                return Optional.empty();
            }
            StackableItemSpawner itemSpawner = new StackableItemSpawner(location, material, owner, delay, spawnChance, toSpawn, maxSize);
            itemSpawner.peers = peers;
            return Optional.of(itemSpawner);
        }

        @Override
        public Optional<OfflineSpawner<StackableItemSpawner>> fromItem(ItemStack itemStack) {
            NBTItem nbtItem = new NBTItem(itemStack.clone());
            String rawClass = nbtItem.getString("class");
            if (rawClass == null) {
                return Optional.empty();
            }
            Class<?> raw;
            try {
                raw = Class.forName(rawClass);
                if (!raw.isAssignableFrom(ItemSpawner.class)) {
                    return Optional.empty();
                }
            } catch (ClassNotFoundException ex) {
                return Optional.empty();
            }
            //Check validity
            float spawnChance;
            int delay;
            UUID owner;
            int version;
            Collection<UUID> peers;
            delay = nbtItem.getInteger("delay");
            spawnChance = nbtItem.getFloat("spawnChance");
            String rawUUID = nbtItem.getString("owner");
            Gson gson = new GsonBuilder().create();
            String rawPeers = nbtItem.getString("peers");
            version = nbtItem.getInteger("classVersion");
            if (rawPeers == null || rawUUID == null) {
                return Optional.empty();
            }
            if (version != VERSION) {
                Optional<ItemStack> optional = convertFromOldVersion(itemStack);
                if (optional.isPresent()) {
                    return fromItem(optional.get());
                } else {
                    return Optional.empty();
                }
            }
            try {
                //Check validity
                owner = UUID.fromString(rawUUID);
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
            Type type = new TypeToken<Collection<UUID>>() {
            }.getType();
            peers = gson.fromJson(rawPeers, type);
            assert peers != null;
            ItemStack toSpawn = nbtItem.getObject("spawnedItem", ItemStack.class);
            if (toSpawn == null) {
                return Optional.empty();
            }
            return Optional.of(new OfflineSpawner<>(StackableItemSpawner.class, itemStack));
        }

        @Override
        public boolean isSpawner(ItemStack itemStack) {
            return fromItem(itemStack).isPresent();
        }

    }

}
