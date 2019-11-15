package com.gmail.andrewandy.spawnerplugin.spawner.stackable;

import com.gmail.andrewandy.corelib.util.gui.Gui;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.spawner.AbstractSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.LivingEntitySpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.OfflineSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.custom.CustomisableSpawner;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import javax.swing.text.html.Option;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;

public class EntitySpawner extends AbstractSpawner implements LivingEntitySpawner, StackableSpawner<EntitySpawner>, CustomisableSpawner {

    private static final int VERSION = 0;
    private final int maxSize;
    private final EntityType spawnedType;
    private Collection<OfflineSpawner<EntitySpawner>> stacked = new HashSet<>();

    public EntitySpawner(Location location, Material material, UUID owner, int delay, EntityType spawnedType, int maxSize) {
        super(location, material, owner, delay);
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
        if (!Objects.requireNonNull(spawnedType).isSpawnable() || spawnedType.getEntityClass() == null) {
            throw new IllegalArgumentException("Invalid EntityType.");
        }
        this.spawnedType = spawnedType;
    }

    public EntitySpawner(Location location, Material material, UUID owner, int delay, float spawnChance, EntityType spawnedType, int maxSize) {
        super(location, material, owner, delay, spawnChance);
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
        if (!Objects.requireNonNull(spawnedType).isSpawnable() || spawnedType.getEntityClass() == null) {
            throw new IllegalArgumentException("Invalid EntityType.");
        }
        this.spawnedType = spawnedType;
    }

    public EntitySpawner(Location location, Material material, UUID owner, int delay, Collection<UUID> peers, EntityType spawnedType, int maxSize) {
        super(location, material, owner, delay, peers);
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
        if (!Objects.requireNonNull(spawnedType).isSpawnable() || spawnedType.getEntityClass() == null) {
            throw new IllegalArgumentException("Invalid EntityType.");
        }
        this.spawnedType = spawnedType;
    }

    public static ItemWrapper<? extends EntitySpawner> getWrapper() {
        return WrapperImpl.getInstance();
    }

    @Override
    public MetadataValue getAsMetadata() {
        return new FixedMetadataValue(SpawnerPlugin.getInstance(), WrapperImpl.getInstance().toItem(this));
    }

    @Override
    public void initialize() {
    }

    @Override
    protected void tick() {
        super.tick();
        if (shouldSpawn()) {
            spawnTick(getLocation().getBlock());
        }
    }

    @Override
    public void spawnTick(Block block) {
        if (block == null) {
            return;
        }
        Optional<Block> nearestAirBlock = nearestAirBlock();
        int spawnAmount = 1;
        for (int i = 0; i < size(); i++) {
            if (shouldSpawn()) {
                spawnAmount++;
            }
        }
        assert spawnedType.getEntityClass() != null;
        if (!nearestAirBlock.isPresent()) {
            updateInvalidLocationDisplay("&cUnable to find location to Spawn", Color.RED, super.delay, 1);
            return;
        }
        while (spawnAmount > 0) {
            block.getWorld().spawn(nearestAirBlock.get().getLocation(), spawnedType.getEntityClass());
            spawnAmount--;
        }
    }

    @Override
    public Optional<Gui> getDisplayUI() {
        return Optional.empty();
    }

    @Override
    public int maxSize() {
        return maxSize;
    }

    @Override
    public Collection<OfflineSpawner<EntitySpawner>> getStacked() {
        return Collections.unmodifiableCollection(stacked);
    }

    @Override
    public void stack(OfflineSpawner<EntitySpawner> spawner) {
        if (size() + 1 == maxSize) {
            throw new IllegalStateException("Stack is full.");
        }
        stacked.add(spawner);
    }

    @Override
    public void remove(OfflineSpawner<EntitySpawner> spawner) {
        stacked.remove(Objects.requireNonNull(spawner));
    }

    @Override
    public boolean stackAll(Collection<OfflineSpawner<EntitySpawner>> offlineSpawners) {
        if (!canStack(Objects.requireNonNull(offlineSpawners))) {
            return false;
        }
        return stacked.addAll(offlineSpawners);
    }

    @Override
    public EntityType getSpawnedType() {
        return spawnedType;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * super.hashCode();
        hash = hash * maxSize;
        hash = hash * spawnedType.hashCode();
        return hash;
    }

    @Override
    public void clear() {
        stacked.clear();
    }

    @Override
    public void removeIf(Predicate<OfflineSpawner<EntitySpawner>> predicate) {
        stacked.removeIf(predicate);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EntitySpawner)) {
            return false;
        }
        EntitySpawner target = (EntitySpawner) o;
        return target.hashCode() == this.hashCode();
    }

    private static class WrapperImpl extends ItemWrapper<EntitySpawner> {

        private static WrapperImpl instance;

        private WrapperImpl() {
        }

        public static WrapperImpl getInstance() {
            if (instance == null) {
                instance = new WrapperImpl();
            }
            return instance;
        }

        @Override
        public ItemStack toItem(EntitySpawner spawner) {
            Objects.requireNonNull(spawner);
            ItemStack itemStack = new ItemStack(spawner.getBlockMaterial());
            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setString("class", spawner.getClass().getName());
            nbtItem.setInteger("classVersion", VERSION);
            nbtItem.setString("owner", spawner.getOwner().toString());
            nbtItem.setInteger("delay", spawner.getDelay());
            nbtItem.setInteger("maxSize", spawner.maxSize());
            nbtItem.setFloat("spawnChance", spawner.getSpawnChance());
            Type type = new TypeToken<Collection<UUID>>() {
            }.getType();
            Gson gson = new GsonBuilder().create();
            Type stackedType = new TypeToken<Collection<OfflineSpawner<EntitySpawner>>>() {
            }.getType();
            nbtItem.setString("peers", gson.toJson(spawner.peers, type));
            nbtItem.setString("stacked", gson.toJson(spawner.stacked, stackedType));
            nbtItem.setString("material", spawner.getBlockMaterial().name());
            nbtItem.setString("spawnedType", spawner.getSpawnedType().name());
            return nbtItem.getItem();
        }

        @Override
        public Optional<OfflineSpawner<EntitySpawner>> fromItem(ItemStack itemStack) {
            if (itemStack == null) {
                return Optional.empty();
            }
            NBTItem nbtItem = new NBTItem(itemStack.clone());
            String rawClass;
            rawClass = nbtItem.getString("class");
            if (rawClass == null) {
                return Optional.empty();
            }
            try {
                Class<?> rawClazz = Class.forName(rawClass);
                if (!rawClazz.isAssignableFrom(EntitySpawner.class)) {
                    return Optional.empty();
                }
            } catch (ClassNotFoundException ex) {
                return Optional.empty();
            }
            int classVersion = nbtItem.getInteger("classVersion");
            int maxSize = nbtItem.getInteger("maxSize");
            String rawOwner = nbtItem.getString("owner");
            String rawMaterial = nbtItem.getString("material");
            String rawSpawnedType = nbtItem.getString("spawnedType");
            if (rawOwner == null || rawMaterial == null || rawSpawnedType == null) {
                return Optional.empty();
            }
            UUID owner;
            Material material;
            EntityType spawnedType;
            try {
                owner = UUID.fromString(rawOwner);
                material = Material.valueOf(rawMaterial);
                spawnedType = EntityType.valueOf(rawSpawnedType);
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
            int delay = nbtItem.getInteger("delay");
            float spawnChance = nbtItem.getFloat("spawnChance");
            String rawPeers = nbtItem.getString("peers");
            String rawStacked = nbtItem.getString("stacked");
            if (rawPeers == null || rawStacked == null) {
                return Optional.empty();
            }
            Type type = new TypeToken<Collection<UUID>>() {
            }.getType();
            Type stackedType = new TypeToken<Collection<OfflineSpawner<EntitySpawner>>>() {
            }.getType();
            Gson gson = new GsonBuilder().create();
            Collection<UUID> peers = gson.fromJson(rawPeers, type);
            Collection<OfflineSpawner<EntitySpawner>> stacked = gson.fromJson(rawStacked, stackedType);
            if (peers == null || stacked == null) {
                return Optional.empty();
            }
            return Optional.of(new OfflineSpawner<>(EntitySpawner.class, itemStack));
        }

        @Override
        public Optional<EntitySpawner> toLiveAtLocation(OfflineSpawner<EntitySpawner> spawner, Location location) {
            if (spawner == null) {
                return Optional.empty();
            }
            Objects.requireNonNull(Objects.requireNonNull(location).getWorld());
            ItemStack itemStack = spawner.getItemStack();
            if (itemStack == null) {
                return Optional.empty();
            }
            NBTItem nbtItem = new NBTItem(itemStack.clone());
            String rawClass;
            rawClass = nbtItem.getString("class");
            if (rawClass == null) {
                return Optional.empty();
            }
            try {
                Class<?> rawClazz = Class.forName(rawClass);
                if (!rawClazz.isAssignableFrom(EntitySpawner.class)) {
                    return Optional.empty();
                }
            } catch (ClassNotFoundException ex) {
                return Optional.empty();
            }
            int classVersion = nbtItem.getInteger("classVersion");
            int maxSize = nbtItem.getInteger("maxSize");
            String rawOwner = nbtItem.getString("owner");
            String rawMaterial = nbtItem.getString("material");
            String rawSpawnedType = nbtItem.getString("spawnedType");
            if (rawOwner == null || rawMaterial == null || rawSpawnedType == null) {
                return Optional.empty();
            }
            UUID owner;
            Material material;
            EntityType spawnedType;
            try {
                owner = UUID.fromString(rawOwner);
                material = Material.valueOf(rawMaterial);
                spawnedType = EntityType.valueOf(rawSpawnedType);
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
            int delay = nbtItem.getInteger("delay");
            float spawnChance = nbtItem.getFloat("spawnChance");
            String rawPeers = nbtItem.getString("peers");
            String rawStacked = nbtItem.getString("stacked");
            if (rawPeers == null || rawStacked == null) {
                return Optional.empty();
            }
            Type type = new TypeToken<Collection<UUID>>() {
            }.getType();
            Type stackedType = new TypeToken<Collection<OfflineSpawner<EntitySpawner>>>() {
            }.getType();
            Gson gson = new GsonBuilder().create();
            Collection<UUID> peers = gson.fromJson(rawPeers, type);
            Collection<OfflineSpawner<EntitySpawner>> stacked = gson.fromJson(rawStacked, stackedType);
            if (peers == null || stacked == null) {
                return Optional.empty();
            }
            EntitySpawner target = new EntitySpawner(location, material, owner, delay, spawnChance, spawnedType, maxSize);
            target.peers = peers;
            target.stacked = stacked;
            return Optional.of(target);
        }

        @Override
        public boolean isSpawner(ItemStack itemStack) {
            return fromItem(itemStack).isPresent();
        }
    }
}
