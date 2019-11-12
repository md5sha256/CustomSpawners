package com.gmail.andrewandy.spawnerplugin.spawner;

import com.gmail.andrewandy.corelib.util.gui.Gui;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
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
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ItemSpawner extends AbstractSpawner {

    private final static int VERSION = 0;
    private final static ItemWrapper<ItemSpawner> WRAPPER = new WrapperImpl();

    private ItemStack toSpawn;

    public ItemSpawner(Location location, Material material, UUID owner, int tickDelay, float spawnChance, ItemStack toSpawn) {
        super(location, material, owner, tickDelay, spawnChance);
        this.toSpawn = Objects.requireNonNull(toSpawn).clone();
    }

    public ItemSpawner(Location location, Material material, UUID owner, int tickDelay, ItemStack toSpawn) {
        this(location, material, owner, tickDelay, toSpawn, null);
    }

    public ItemSpawner(Location location, Material material, UUID owner, int tickDelay, ItemStack toSpawn, Collection<UUID> peers) {
        super(location, material, owner, tickDelay, peers);
        this.toSpawn = Objects.requireNonNull(toSpawn).clone();
    }

    @Override
    public BlockState getAsBlockState() {
        BlockState blockState = getLocation().getBlock().getState(true);
        MetadataValue value = new FixedMetadataValue(SpawnerPlugin.getInstance(), WRAPPER.toItem(this));
        blockState.setMetadata("itemSpawnerData", value);
        return blockState;
    }

    private static Optional<ItemStack> convertFromOldVersion(ItemStack itemStack) {
        //Since this is version 0, there is no old version.
        return Optional.of(itemStack);
    }

    public boolean isSpawner(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack.clone());
        String rawClass = nbtItem.getString("class");
        if (rawClass == null) {
            return false;
        }
        try {
            Class<?> clazz = Class.forName(rawClass);
            return clazz.isAssignableFrom(this.getClass());

        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    @Override
    public Optional<Gui> getDisplayUI() {
        return Optional.empty();
    }

    @Override
    public void addPeer(UUID peer) {
        this.peers.add(Objects.requireNonNull(peer));
    }

    @Override
    public void removePeer(UUID peer) {
        this.peers.remove(Objects.requireNonNull(peer));
    }

    @Override
    public void initialize() {
        //Nothing needs to be initialized.
        return;
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

    public static ItemWrapper<? extends ItemSpawner> getWrapper() {
        return WRAPPER;
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
            double spawnChance;
            int delay;
            UUID owner;
            int version;
            Collection<UUID> peers;
            delay = nbtItem.getInteger("delay");
            spawnChance = nbtItem.getDouble("spawnChance");
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

    /*
    private static class HandlerImpl implements Handler<ItemSpawner> {
        private Map<ItemSpawner, BukkitTask> spawners = new HashMap<>();

        @Override
        public void register(ItemSpawner spawner) {
            if (!isRegisteredSpawner(spawner.location)) {
                BukkitRunnable runnable = Common.asBukkitRunnable(() -> {
                    spawner.getAsBlockState().update(true);
                    spawner.tick();
                });
                this.spawners.put(spawner, runnable.runTaskTimer(SpawnerPlugin.getInstance(), 0, spawner.getDelay()));
            }
        }

        @Override
        public void unregister(ItemSpawner spawner) {
            if (!isRegisteredSpawner(spawner.location)) {
                return;
            }
            unregister(spawner.location);
        }

        @Override
        public void unregister(Location location) {
            Objects.requireNonNull(location);
            location = location.clone();
            Optional<ItemSpawner> optional = (this.spawners.keySet().stream().filter(spawner1 -> spawner1.location.equals(location)).findAny());
            optional.ifPresent((spawner1) -> {
                this.spawners.get(spawner1).cancel();
                this.spawners.remove(spawner1);
            });
        }

        @Override
        public boolean isRegisteredSpawner(Location location) {
            Objects.requireNonNull(location);
            return this.spawners.keySet().stream().anyMatch((spawner -> spawner.location.equals(location)));
        }

        @Override
        public Collection<ItemSpawner> getRegistered() {
            return Collections.unmodifiableCollection(spawners.keySet());
        }

        @Override
        public boolean contains(ItemSpawner spawner) {
            return this.spawners.containsKey(spawner);
        }
    }
    */
