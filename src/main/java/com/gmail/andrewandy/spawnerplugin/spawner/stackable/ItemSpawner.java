package com.gmail.andrewandy.spawnerplugin.spawner.stackable;

import com.gmail.andrewandy.corelib.util.gui.Gui;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.spawner.AbstractSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.ItemStackSpawner;
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

public class ItemSpawner extends AbstractSpawner implements ItemStackSpawner, StackableSpawner<ItemSpawner> {

    private final static int VERSION = 0;
    private final static ItemWrapper<ItemSpawner> WRAPPER = new WrapperImpl();
    private final int maxSize;
    private Collection<OfflineSpawner<ItemSpawner>> stacked = new HashSet<>();

    private ItemStack toSpawn;

    public ItemSpawner(Location location, Material material, UUID owner, int tickDelay, float spawnChance, ItemStack toSpawn, int maxSize) {
        super(location, material, owner, tickDelay, spawnChance);
        this.toSpawn = Objects.requireNonNull(toSpawn).clone();
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
    }

    public ItemSpawner(Location location, Material material, UUID owner, int tickDelay, ItemStack toSpawn, int maxSize) {
        this(location, material, owner, tickDelay, toSpawn, null, maxSize);
    }

    public ItemSpawner(Location location, Material material, UUID owner, int tickDelay, ItemStack toSpawn, Collection<UUID> peers, int maxSize) {
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

    public static ItemWrapper<? extends ItemSpawner> getWrapper() {
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
    public Collection<OfflineSpawner<ItemSpawner>> getStacked() {
        return Collections.unmodifiableCollection(stacked);
    }

    @Override
    public void stack(OfflineSpawner<ItemSpawner> spawner) {
        if (isFull()) {
            return;
        }
        stacked.add(spawner);
    }

    @Override
    public void remove(OfflineSpawner<ItemSpawner> spawner) {
        stacked.remove(spawner);
    }

    @Override
    public int maxSize() {
        return maxSize;
    }

    @Override
    public boolean stackAll(Collection<OfflineSpawner<ItemSpawner>> collection) {
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
        if (!(o instanceof ItemSpawner)) {
            return false;
        }
        ItemSpawner target = (ItemSpawner) o;
        return target.hashCode() == this.hashCode();
    }

    private static class WrapperImpl extends ItemWrapper<ItemSpawner> {

        WrapperImpl() {
        }

        @Override
        public ItemStack toItem(ItemSpawner spawner) {
            if (spawner == null) {
                throw new IllegalArgumentException("Spawner cannot be null.");
            }
            ItemStack itemStack = new ItemStack(Material.SPAWNER);
            NBTItem nbtItem = new NBTItem(itemStack);
            Gson gson = new GsonBuilder().create();
            nbtItem.setString("class", ItemSpawner.class.getName());
            nbtItem.setInteger("classVersion", ItemSpawner.VERSION);
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
        public Optional<ItemSpawner> place(OfflineSpawner<ItemSpawner> spawner, Location location) {
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
                    Optional<OfflineSpawner<ItemSpawner>> offlineSpawner = fromItem(optional.get());
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
            ItemSpawner itemSpawner = new ItemSpawner(location, material, owner, delay, spawnChance, toSpawn, maxSize);
            itemSpawner.peers = peers;
            return Optional.of(itemSpawner);
        }

        @Override
        public Optional<OfflineSpawner<ItemSpawner>> fromItem(ItemStack itemStack) {
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
            return Optional.of(new OfflineSpawner<>(ItemSpawner.class, itemStack));
        }

        @Override
        public boolean isSpawner(ItemStack itemStack) {
            return fromItem(itemStack).isPresent();
        }

    }

}
