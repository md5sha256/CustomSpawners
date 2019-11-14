package com.gmail.andrewandy.spawnerplugin.spawner;

import com.gmail.andrewandy.corelib.util.gui.Gui;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Shulker;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeWrapper;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public abstract class AbstractSpawner implements Spawner {

    private final static Map<Class<? extends AbstractSpawner>, Handler> handlerMap = new HashMap<>();

    static {
        handlerMap.put(AbstractSpawner.class, HandlerImpl.getInstance());
    }

    protected final int delay;
    protected final UUID owner;
    protected final Material material;
    private final Location location;
    protected Collection<UUID> peers;
    private float spawnChance = 1.00F;
    private UUID shulkerDisplay;

    public AbstractSpawner(Location location, Material material, UUID owner, int delay) {
        this(location, material, owner, delay, null);
    }

    public AbstractSpawner(Location location, Material material, UUID owner, int delay, float spawnChance) {
        this(location, material, owner, delay, null);

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

    public static void registerHandler(Class<? extends AbstractSpawner> clazz, Handler handler) {
        synchronized (handlerMap) {
            handlerMap.put(Objects.requireNonNull(clazz), Objects.requireNonNull(handler));
        }
    }

    public static void unregisterHandler(Class<? extends AbstractSpawner> clazz) {
        synchronized (handlerMap) {
            if (handlerMap.containsKey(Objects.requireNonNull(clazz))) {
                handlerMap.remove(clazz);
            }
        }
    }

    public static ItemWrapper<? extends AbstractSpawner> getWrapper() {
        throw new UnsupportedOperationException("Subclass must hide this method.");
    }

    @Override
    public Location getLocation() {
        return location;
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

    @Override
    public abstract BlockState getAsBlockState();

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

    protected void tick() {
        //Check if the spawner is still there, if not clear it using the handler.
        if (shulkerDisplay == null && location.getBlock().getType() != material) {
            Common.log(Level.WARNING, "&cTicked a removed spawner.");
            //Spawner.Handler is synchronized on editing, therefore this is all safe.
            Location location = this.location.clone();
            BukkitRunnable runnable = Common.asBukkitRunnable(() -> {
                Class<?> lastClass = this.getClass();
                while (true) {
                    Class<? extends AbstractSpawner> clazz = lastClass.asSubclass(AbstractSpawner.class);
                    Handler handler = handlerMap.get(clazz);
                    if (handler != null) {
                        //Reason for not breaking, is to throughly unregister all classes.
                        handler.unregister(location);
                    }
                    if (!clazz.equals(AbstractSpawner.class)) {
                        lastClass = clazz.getSuperclass();
                        if (lastClass.equals(AbstractSpawner.class)) {
                            handlerMap.get(AbstractSpawner.class).unregister(this);
                            break;
                        } else {
                            lastClass = clazz;
                        }
                    }
                }
            });
            runnable.runTaskAsynchronously(SpawnerPlugin.getInstance());
            return;
        }
        if (inInvalidLocation()) {
            Shulker shulker = (Shulker) Bukkit.getEntity(shulkerDisplay);
            if (shulker != null && shulker.hasPotionEffect(PotionEffectType.GLOWING)) {
                return;
            }
            updateInvalidLocationDisplay("&c&lUnable to find location to spawn item.", Color.RED, delay + 2, 2);
        }
    }

    public void destroy() {
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

    public abstract Optional<Gui> getDisplayUI();

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

    protected interface Handler<T extends AbstractSpawner> {

        void register(T spawner);

        void unregister(T spawner);

        void unregister(Location location);

        boolean isRegisteredSpawner(Location location);

        Collection<T> getRegistered();

        boolean contains(T spawner);

    }

    private final static class HandlerImpl implements Handler<AbstractSpawner> {
        private static Handler instance;
        private Map<AbstractSpawner, BukkitTask> spawners = new HashMap<>();

        private HandlerImpl() {
        }

        public static Handler getInstance() {
            if (instance == null) {
                instance = new HandlerImpl();
            }
            return instance;
        }

        @Override
        public void register(AbstractSpawner spawner) {
            Objects.requireNonNull(spawner);
            if (!isRegisteredSpawner(spawner.location)) {
                BukkitRunnable runnable = Common.asBukkitRunnable(() -> {
                    spawner.getAsBlockState().update(true);
                    spawner.tick();
                });
                this.spawners.put(spawner, runnable.runTaskTimer(SpawnerPlugin.getInstance(), 0, spawner.getDelay()));
            }
        }

        @Override
        public void unregister(AbstractSpawner spawner) {
            Objects.requireNonNull(spawner);
            if (spawners.containsKey(spawner)) {
                BukkitTask task = spawners.get(spawner);
                task.cancel();
                spawner.destroy();
                spawners.remove(spawner);
            }
        }

        @Override
        public boolean contains(AbstractSpawner spawner) {
            return spawners.containsKey(spawner);
        }

        @Override
        public void unregister(Location location) {
            Objects.requireNonNull(Objects.requireNonNull(location).getWorld());
            Optional<AbstractSpawner> optional = spawners.keySet().stream().filter((spawner) -> spawner.location.equals(location)).findFirst();
            optional.ifPresent(this::unregister);
        }

        @Override
        public boolean isRegisteredSpawner(Location location) {
            return spawners.keySet().stream().anyMatch((spawner) -> spawner.location.equals(location));
        }

        @Override
        public Collection<AbstractSpawner> getRegistered() {
            return Collections.unmodifiableCollection(spawners.keySet());
        }
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
