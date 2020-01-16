package com.gmail.andrewandy.spawnerplugin.spawner;

import com.gmail.andrewandy.corelib.api.menu.Menu;
import com.gmail.andrewandy.corelib.util.Common;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Shulker;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeWrapper;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public abstract class AbstractSpawner implements Spawner {

    protected final int delay;
    protected final UUID owner;
    protected final Material material;
    private final Location location;
    protected Collection<UUID> peers;
    private float spawnChance;
    private UUID shulkerDisplay;
    private boolean invulernable;

    public AbstractSpawner(Location location, Material material, UUID owner, int delay) {
        this(location, material, owner, delay, 1.00F);
    }

    public AbstractSpawner(Location location, Material material, UUID owner, int delay, float spawnChance) {
        this(location, material, owner, delay, null);
        if (spawnChance < -0.001 || spawnChance > 1.001) {
            throw new IllegalArgumentException("Invalid SpawnChance.");
        }
        this.spawnChance = spawnChance;
    }

    public AbstractSpawner(Location location, Material material, UUID owner, int delay, Collection<UUID> peers) {
        if (delay < 1) {
            throw new IllegalArgumentException("delay must be greater than zero.");
        }
        Objects.requireNonNull(Objects.requireNonNull(location).getWorld());
        if (material == null || !material.isBlock()) {
            throw new IllegalArgumentException("Invalid material.");
        }
        this.material = material;
        this.location = location;
        this.delay = delay;
        this.owner = Objects.requireNonNull(owner);
        if (peers == null) {
            this.peers = new HashSet<>();
        } else {
            this.peers = new HashSet<>(peers);
        }
        this.peers.remove(owner);
    }

    public static ItemWrapper<? extends AbstractSpawner> getWrapper() {
        throw new UnsupportedOperationException("Subclass must hide this method.");
    }

    public boolean isInvulernable() {
        return invulernable;
    }

    public void setInvulernable(boolean invulernable) {
        this.invulernable = invulernable;
    }

    public void unregister(SpawnerManager spawnerManager, boolean clearData) {
        Objects.requireNonNull(spawnerManager).unregisterSpawner(this.location, clearData);
    }

    public void unregister(SpawnerManager spawnerManager) {
        unregister(spawnerManager, false);
    }

    public void unregister() {
        unregister(Spawners.defaultManager(), false);
    }

    public void unregister(boolean clearData) {
        unregister(Spawners.defaultManager(), clearData);
    }

    public void registerToManager(SpawnerManager spawnerManager) {
        Objects.requireNonNull(spawnerManager).registerSpawner(this);
    }

    public void register() {
        registerToManager(Spawners.defaultManager());
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public float getSpawnChance() {
        return spawnChance;
    }

    @Override
    public void setSpawnChance(float spawnChance) {
        if (spawnChance > 1 || spawnChance < 0) {
            throw new IllegalArgumentException("SpawnChance must be between 0 and 1");
        }
        this.spawnChance = spawnChance;
    }

    public boolean shouldSpawn() {
        Random random = ThreadLocalRandom.current();
        return random.nextDouble() < spawnChance;
    }

    @Override
    public Material getBlockMaterial() {
        return material;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public int getDelay() {
        return delay;
    }

    @Override
    public Collection<UUID> getPeers() {
        return new HashSet<>(peers);
    }

    public void updateBlockState() {
        MetadataValue metaData = getAsMetadata();
        List<MetadataValue> existing = getLocation().getBlock().getMetadata(Spawners.DEFAULT_META_KEY);
        existing.forEach(MetadataValue::invalidate);
        getLocation().getBlock().setMetadata(Spawners.DEFAULT_META_KEY, metaData);
    }

    public abstract MetadataValue getAsMetadata();

    @Override
    public void addPeer(UUID peer) {
        if (Objects.requireNonNull(peer).equals(owner)) {
            return;
        }
        this.peers.add(peer);
    }

    @Override
    public void removePeer(UUID peer) {
        this.peers.remove(Objects.requireNonNull(peer));
    }

    public abstract void initialize();

    public void tick() {
        //Check if the spawner is still there, if not clear it using the handler.
        if (shulkerDisplay == null && location.getBlock().getType() != material) {
            Common.log(Level.WARNING, "&cTicked a destoryed spawner.");
            Spawners.defaultManager().unregisterSpawner(this.location.clone());
            return;
        }
        if (currentLocationInvalid()) {
            Shulker shulker = (Shulker) Bukkit.getEntity(shulkerDisplay);
            if (shulker != null && shulker.hasPotionEffect(PotionEffectType.GLOWING)) {
                return;
            }
            updateInvalidLocationDisplay("&c&lUnable to find location to spawn item.", Color.RED, delay + 2, 2);
        }
        updateBlockState();
    }

    public void clearShulkerDisplay() {
        Shulker shulker = (Shulker) Bukkit.getEntity(shulkerDisplay);
        if (shulker != null) {
            //Kill the displayShulker;
            shulker.damage(shulker.getHealth());
        }
    }

    public void updateInvalidLocationDisplay(String message, Color edgeColour, int duration, int amplifier) {
        PotionEffect potionEffect = new PotionEffect(new CustomPotionEffect(edgeColour).getType(), duration, amplifier);
        PotionEffect invisible = new PotionEffect(PotionEffectType.INVISIBILITY, duration, amplifier);
        Shulker shulker = (Shulker) location.getWorld().spawnEntity(location, EntityType.SHULKER);
        shulker.setInvulnerable(true);
        shulker.addPotionEffect(invisible, true);
        shulker.setCustomNameVisible(true);
        shulker.setCustomName(Common.colourise(message));
        shulker.setGlowing(true);
        shulker.addPotionEffect(potionEffect, true);
        shulkerDisplay = shulker.getUniqueId();
        Bukkit.getScheduler().runTaskLater(SpawnerPlugin.getInstance(), () -> {
            if (shulker.hasPotionEffect(PotionEffectType.GLOWING)) {
                shulker.setCustomName("");
                shulker.setCustomNameVisible(false);
            }
        }, duration);
    }

    public abstract Optional<Menu> getDisplayUI();

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AbstractSpawner)) {
            return false;
        }
        AbstractSpawner target = (AbstractSpawner) o;
        boolean equalShulker;
        if (this.shulkerDisplay == null) {
            if (target.shulkerDisplay == null) {
                equalShulker = true;
            } else {
                equalShulker = false;
            }
        } else {
            if (target.shulkerDisplay == null) {
                equalShulker = false;
            } else {
                equalShulker = target.shulkerDisplay.equals(this.shulkerDisplay);
            }
        }
        return target.delay == this.delay && target.location.equals(this.location) && target.owner.equals(this.owner) && target.peers.equals(this.peers) && target.material == this.material
                && target.spawnChance == this.spawnChance && equalShulker;
    }

    @Override
    public int hashCode() {
        int hash = 19;
        hash = hash * delay;
        hash = hash * location.hashCode();
        hash = hash * owner.hashCode();
        hash = hash * peers.hashCode();
        hash = hash * material.hashCode();
        hash = hash * (int) (100 * spawnChance);
        return hash;
    }

    private static class CustomPotionEffect extends PotionEffectTypeWrapper {

        private final Color color;

        public CustomPotionEffect(Color displayColour) {
            super(24);
            this.color = Objects.requireNonNull(displayColour);
        }

        @Override
        public double getDurationModifier() {
            return 0;
        }

        @Override
        public String getName() {
            return "CustomPotionEffect";
        }

        @Override
        public boolean isInstant() {
            return getType().isInstant();
        }

        @Override
        public Color getColor() {
            return color;
        }
    }
}
