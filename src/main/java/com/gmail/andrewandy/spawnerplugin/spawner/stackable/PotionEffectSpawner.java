package com.gmail.andrewandy.spawnerplugin.spawner.stackable;

import com.gmail.andrewandy.corelib.util.Common;
import com.gmail.andrewandy.corelib.util.gui.Gui;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.spawner.OfflineSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.custom.AbstractCustomizableSpawner;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

public class PotionEffectSpawner extends AbstractCustomizableSpawner implements StackableSpawner<PotionEffectSpawner> {

    private static final int VERSION = 0;
    private final PotionEffect spawnedEffect;
    private final boolean lingering;
    private final int maxSize;
    private Collection<OfflineSpawner<PotionEffectSpawner>> stacked = new HashSet<>();

    public PotionEffectSpawner(Location location, Material material, UUID owner, int delay, PotionEffect spawnedEffect, boolean lingering, int maxSize) {
        super(location, material, owner, delay);
        this.spawnedEffect = Objects.requireNonNull(spawnedEffect);
        this.lingering = lingering;
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
    }

    public PotionEffectSpawner(Location location, Material material, UUID owner, int delay, float spawnChance, PotionEffect spawnedEffect, boolean lingering, int maxSize) {
        super(location, material, owner, delay, spawnChance);
        this.spawnedEffect = Objects.requireNonNull(spawnedEffect);
        this.lingering = lingering;
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
    }

    public PotionEffectSpawner(Location location, Material material, UUID owner, int delay, Collection<UUID> peers, PotionEffect spawnedEffect, boolean lingering, int maxSize) {
        super(location, material, owner, delay, peers);
        this.spawnedEffect = Objects.requireNonNull(spawnedEffect);
        this.lingering = lingering;
        if (maxSize < 1) {
            throw new IllegalArgumentException("MaxSize must be greater than 0.");
        }
        this.maxSize = maxSize;
    }

    public static ItemWrapper<? extends PotionEffectSpawner> getWrapper() {
        return WrapperImpl.getInstance();
    }

    @Override
    public MetadataValue getAsMetadata() {
        return new FixedMetadataValue(SpawnerPlugin.getInstance(), WrapperImpl.getInstance().toItem(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (shouldSpawn()) {
            Optional<Block> block = nearestAirBlock();
            block.ifPresent(this::spawnTick);
        }
    }

    @Override
    public void spawnTick(Block block) {
        ThrownPotion thrownPotion = (ThrownPotion) block.getWorld().spawnEntity(block.getLocation().add(0, 1, 0), EntityType.SPLASH_POTION);
        Collection<PotionEffect> effects = thrownPotion.getEffects();
        effects.clear();
        effects.add(getSpawnedEffect());
        thrownPotion.setSilent(true);
        thrownPotion.setGravity(true);
        thrownPotion.setInvulnerable(false);
    }


    @Override
    public void initialize() {
    }

    @Override
    public Optional<Gui> getDisplayUI() {
        return Optional.empty();
    }

    @Override
    public Collection<OfflineSpawner<PotionEffectSpawner>> getStacked() {
        return Collections.unmodifiableCollection(stacked);
    }

    @Override
    public void stack(OfflineSpawner<PotionEffectSpawner> spawner) {
        if (!isFull()) {
            stacked.add(Objects.requireNonNull(spawner));
        }
    }

    @Override
    public void remove(OfflineSpawner<PotionEffectSpawner> spawner) {
        if (spawner != null) {
            stacked.remove(spawner);
        }
    }

    @Override
    public int maxSize() {
        return maxSize;
    }

    @Override
    public void removeIf(Predicate<OfflineSpawner<PotionEffectSpawner>> predicate) {
        stacked.removeIf(Objects.requireNonNull(predicate));
    }

    public PotionEffect getSpawnedEffect() {
        return spawnedEffect;
    }

    @Override
    public void clear() {
        stacked.clear();
    }

    public boolean isLingering() {
        return lingering;
    }

    @Override
    public boolean stackAll(Collection<OfflineSpawner<PotionEffectSpawner>> offlineSpawners) {
        if (!canStack(offlineSpawners)) {
            return false;
        }
        return stacked.addAll(offlineSpawners);
    }

    private static class WrapperImpl extends ItemWrapper<PotionEffectSpawner> {

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
        public ItemStack toItem(PotionEffectSpawner spawner) {
            Objects.requireNonNull(spawner);
            ItemStack itemStack = new ItemStack(spawner.getBlockMaterial());
            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setString("class", PotionEffectSpawner.class.getName());
            nbtItem.setInteger("classVersion", VERSION);
            nbtItem.setString("owner", spawner.getOwner().toString());
            nbtItem.setInteger("delay", spawner.getDelay());
            nbtItem.setFloat("spawnChance", spawner.getSpawnChance());
            nbtItem.setString("rawMaterial", spawner.getBlockMaterial().name());
            nbtItem.setInteger("maxSize", spawner.maxSize());
            nbtItem.setBoolean("lingering", spawner.isLingering());
            nbtItem.setObject("spawnedEffect", spawner.getSpawnedEffect());
            Gson gson = new GsonBuilder().create();
            Type type = new TypeToken<Collection<UUID>>() {
            }.getType();
            Type stackedType = new TypeToken<Collection<OfflineSpawner<PotionEffectSpawner>>>() {
            }.getType();
            nbtItem.setString("stacked", gson.toJson(spawner.stacked, stackedType));
            nbtItem.setString("peers", gson.toJson(spawner.getPeers(), type));
            return nbtItem.getItem();
        }

        @Override
        public Optional<OfflineSpawner<PotionEffectSpawner>> fromItem(ItemStack itemStack) {
            if (itemStack == null) {
                return Optional.empty();
            }
            NBTItem nbtItem = new NBTItem(itemStack.clone());
            String rawClass;
            int classVersion;
            String rawOwner;
            String rawStacked;
            String rawPeers;
            int maxSize;
            int delay;
            boolean lingering;
            float spawnChance;
            String rawMaterial;
            PotionEffect potionEffect;
            rawClass = nbtItem.getString("class");
            if (rawClass == null) {
                return Optional.empty();
            }
            try {
                Class<?> rawClazz = Class.forName(rawClass);
                if (!rawClazz.isAssignableFrom(PotionEffectSpawner.class)) {
                    return Optional.empty();
                }
            } catch (ClassNotFoundException ex) {
                return Optional.empty();
            }
            classVersion = nbtItem.getInteger("classVersion");
            spawnChance = nbtItem.getFloat("spawnChance");
            delay = nbtItem.getInteger("delay");
            rawOwner = nbtItem.getString("owner");
            rawMaterial = nbtItem.getString("rawMaterial");
            if (rawOwner == null || rawMaterial == null) {
                return Optional.empty();
            }
            UUID owner;
            Material material;
            try {
                owner = UUID.fromString(rawOwner);
                material = Material.valueOf(rawMaterial);
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
            rawPeers = nbtItem.getString("peers");
            rawStacked = nbtItem.getString("rawStacked");
            Collection<UUID> peers;
            Collection<OfflineSpawner<PotionEffectSpawner>> stacked;
            if (rawPeers == null || rawStacked == null) {
                return Optional.empty();
            }
            Type type = new TypeToken<Collection<UUID>>() {
            }.getType();
            Type stackedType = new TypeToken<Collection<OfflineSpawner<PotionEffectSpawner>>>() {
            }.getType();
            Gson gson = new GsonBuilder().create();
            peers = gson.fromJson(rawPeers, type);
            stacked = gson.fromJson(rawStacked, stackedType);
            if (peers == null) {
                return Optional.empty();
            }
            maxSize = nbtItem.getInteger("maxSize");
            lingering = nbtItem.getBoolean("lingering");
            potionEffect = nbtItem.getObject("spawnedEffect", PotionEffect.class);
            if (potionEffect == null) {
                Common.getLogger(SpawnerPlugin.getInstance()).log(Level.WARNING, "&eSpawned Effect is null.");
                return Optional.empty();
            }
            return Optional.of(new OfflineSpawner<>(PotionEffectSpawner.class, itemStack));
        }

        @Override
        public Optional<PotionEffectSpawner> toLiveAtLocation(OfflineSpawner<PotionEffectSpawner> spawner, Location location) {
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
            int classVersion;
            String rawOwner;
            String rawMaterial;
            String rawPeers;
            String rawStacked;
            int maxSize;
            int delay;
            float spawnChance;
            boolean lingering;
            PotionEffect potionEffect;
            rawClass = nbtItem.getString("class");
            if (rawClass == null) {
                return Optional.empty();
            }
            try {
                Class<?> rawClazz = Class.forName(rawClass);
                if (!rawClazz.isAssignableFrom(PotionEffectSpawner.class)) {
                    return Optional.empty();
                }
            } catch (ClassNotFoundException ex) {
                return Optional.empty();
            }
            spawnChance = nbtItem.getFloat("spawnChance");
            classVersion = nbtItem.getInteger("classVersion");
            rawOwner = nbtItem.getString("owner");
            rawMaterial = nbtItem.getString("rawMaterial");
            if (rawOwner == null || rawMaterial == null) {
                return Optional.empty();
            }
            UUID owner;
            Material material;
            try {
                owner = UUID.fromString(rawOwner);
                material = Material.valueOf(rawMaterial);
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
            rawPeers = nbtItem.getString("peers");
            rawStacked = nbtItem.getString("stacked");
            if (rawPeers == null || rawStacked == null) {
                return Optional.empty();
            }
            Collection<UUID> peers;
            Collection<OfflineSpawner<PotionEffectSpawner>> stacked;
            Type type = new TypeToken<Collection<UUID>>() {
            }.getType();
            Type stackedType = new TypeToken<Collection<OfflineSpawner<PotionEffectSpawner>>>() {
            }.getType();
            Gson gson = new GsonBuilder().create();
            peers = gson.fromJson(rawPeers, type);
            stacked = gson.fromJson(rawStacked, stackedType);
            if (peers == null || stacked == null) {
                return Optional.empty();
            }
            delay = nbtItem.getInteger("delay");
            maxSize = nbtItem.getInteger("maxSize");
            lingering = nbtItem.getBoolean("lingering");
            potionEffect = nbtItem.getObject("spawnedEffect", PotionEffect.class);
            if (potionEffect == null) {
                Common.getLogger(SpawnerPlugin.getInstance()).log(Level.WARNING, "&eSpawned Effect is null.");
                return Optional.empty();
            }
            PotionEffectSpawner target = new PotionEffectSpawner(location, material, owner, delay, spawnChance, potionEffect, lingering, maxSize);
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
