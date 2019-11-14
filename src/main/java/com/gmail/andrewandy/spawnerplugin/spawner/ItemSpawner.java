package com.gmail.andrewandy.spawnerplugin.spawner;

import com.gmail.andrewandy.corelib.util.gui.Gui;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.spawner.stackable.StackableItemSpawner;
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

public class ItemSpawner extends StackableItemSpawner {

    private final static int VERSION = 0;
    private final static ItemWrapper<ItemSpawner> WRAPPER = new WrapperImpl();

    public ItemSpawner(Location location, Material material, UUID owner, int tickDelay, float spawnChance, ItemStack toSpawn) {
        super(location, material, owner, tickDelay, spawnChance, toSpawn, 1);
    }

    public ItemSpawner(Location location, Material material, UUID owner, int tickDelay, ItemStack toSpawn) {
        this(location, material, owner, tickDelay, toSpawn, null);
    }

    public ItemSpawner(Location location, Material material, UUID owner, int tickDelay, ItemStack toSpawn, Collection<UUID> peers) {
        super(location, material, owner, tickDelay, toSpawn, peers, 1);
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

    private static class WrapperImpl extends ItemWrapper<ItemSpawner> {

        WrapperImpl() {
        }

        @SuppressWarnings("unchecked")
        private ItemWrapper<StackableItemSpawner> wrapper = (ItemWrapper<StackableItemSpawner>) StackableItemSpawner.getWrapper();

        @Override
        public ItemStack toItem(ItemSpawner spawner) {
            return wrapper.toItem(spawner);
        }

        @Override
        public Optional<ItemSpawner> place(OfflineSpawner<ItemSpawner> spawner, Location location) {
            Objects.requireNonNull(spawner);
            OfflineSpawner<StackableItemSpawner> offlineSpawner = new OfflineSpawner<>(StackableItemSpawner.class, spawner.getItemStack());
            Optional<StackableItemSpawner> optional = wrapper.place(offlineSpawner, location);
            if (!optional.isPresent()) {
                return Optional.empty();
            }
            StackableItemSpawner target = optional.get();
            ItemSpawner itemSpawner = new ItemSpawner(location, target.material, target.owner, target.delay, target.getSpawnChance(), target.getSpawnedItem());
            itemSpawner.peers = target.peers;
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