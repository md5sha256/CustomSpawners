package com.gmail.andrewandy.spawnerplugin.spawner;

import com.gmail.andrewandy.corelib.util.gui.Gui;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeWrapper;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public abstract class Spawner {

    protected final int delay;
    protected final UUID owner;
    protected final Material material;
    private final static Map<Class<? extends Spawner>, Handler> handlerMap = new HashMap<>();
    private double spawnChance = 1.00;

    static {
        handlerMap.put(Spawner.class, HandlerImpl.getInstance());
    }

    private UUID shulkerDisplay;
    Collection<UUID> peers;
    public final Location location;

    public static void registerHandler(Class<? extends Spawner> clazz, Handler handler) {
        synchronized (handlerMap) {
            handlerMap.put(Objects.requireNonNull(clazz), Objects.requireNonNull(handler));
        }
    }

    public static void unregisterHandler(Class<? extends Spawner> clazz) {
        synchronized (handlerMap) {
            if (handlerMap.containsKey(Objects.requireNonNull(clazz))) {
                handlerMap.remove(clazz);
            }
        }
    }

    public Spawner(Location location, Material material, UUID owner, int delay) {
        this(location, material, owner, delay, null);
    }

    public Spawner(Location location, Material material, UUID owner, int delay, double spawnChance) {
        this(location, material, owner, delay, null);

    }

    public void setSpawnChance(double spawnChance) {
        if (spawnChance > 1 || spawnChance < 0) {
            throw new IllegalArgumentException("SpawnChance must be between 0 and 1");
        }
        this.spawnChance = spawnChance;
    }

    public double getSpawnChance() {
        return spawnChance;
    }

    public boolean shouldSpawn() {
        Random random = ThreadLocalRandom.current();
        return random.nextDouble() < spawnChance;
    }

    public Spawner(Location location, Material material, UUID owner, int delay, Collection<UUID> peers) {
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

    public UUID getOwner() {
        return owner;
    }

    public int getDelay() {
        return delay;
    }

    public Collection<UUID> getPeers() {
        return new HashSet<>(peers);
    }

    public abstract BlockState getAsBlockState();

    public boolean isPeer(UUID uuid) {
        return getPeers().contains(uuid);
    }

    public void addPeer(UUID peer) {
        if (Objects.requireNonNull(peer).equals(owner)) {
            return;
        }
        this.peers.add(peer);
    }

    public void removePeer(UUID peer) {
        this.peers.remove(Objects.requireNonNull(peer));
    }

    public abstract void initialize();

    abstract boolean isSpawner(ItemStack itemStack);

    public static <T extends Spawner> ItemWrapper<T> getWrapper() {
        throw new UnsupportedOperationException("Subclass must hide this method.");
    }

    public boolean inInvalidLocation() {
        Block block = location.getBlock();
        return block.getRelative(BlockFace.UP).getType().isAir()
                && block.getRelative(BlockFace.DOWN).getType().isAir()
                && block.getRelative(BlockFace.EAST).getType().isAir()
                && block.getRelative(BlockFace.WEST).getType().isAir();
    }

    protected void tick() {
        //Check if the spawner is still there, if not clear it using the handler.
        if (shulkerDisplay == null && location.getBlock().getType() != material) {
            Common.log(Level.WARNING, "&cTicked a removed spawner.");
            //Spawner.Handler is synchronized on editing, therefore this is all safe.
            Location location = this.location.clone();
            BukkitRunnable runnable = Common.asBukkitRunnable(() -> {
                Class<?> lastClass = this.getClass();
                while (true) {
                    Class<? extends Spawner> clazz = lastClass.asSubclass(Spawner.class);
                    Handler handler = handlerMap.get(clazz);
                    if (handler != null) {
                        //Reason for not breaking, is to throughly unregister all classes.
                        handler.unregister(location);
                    }
                    if (!clazz.equals(Spawner.class)) {
                        lastClass = clazz.getSuperclass();
                        if (lastClass.equals(Spawner.class)) {
                            handlerMap.get(Spawner.class).unregister(this);
                            break;
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


    private final static class HandlerImpl implements Handler<Spawner> {
        private Map<Spawner, BukkitTask> spawners = new HashMap<>();
        private static Handler instance;

        private HandlerImpl() {
        }

        public static Handler getInstance() {
            if (instance == null) {
                instance = new HandlerImpl();
            }
            return instance;
        }

        @Override
        public void register(Spawner spawner) {
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
        public void unregister(Spawner spawner) {
            Objects.requireNonNull(spawner);
            if (spawners.containsKey(spawner)) {
                BukkitTask task = spawners.get(spawner);
                task.cancel();
                spawners.remove(spawner);
            }
        }

        @Override
        public boolean contains(Spawner spawner) {
            return spawners.containsKey(spawner);
        }

        @Override
        public void unregister(Location location) {
            Objects.requireNonNull(Objects.requireNonNull(location).getWorld());
            Optional<Spawner> optional = spawners.keySet().stream().filter((spawner) -> spawner.location.equals(location)).findFirst();
            optional.ifPresent((spawner) -> {
                spawners.get(spawner).cancel();
                spawners.remove(spawner);
            });
        }

        @Override
        public boolean isRegisteredSpawner(Location location) {
            return spawners.keySet().stream().anyMatch((spawner) -> spawner.location.equals(location));
        }

        @Override
        public Collection<Spawner> getRegistered() {
            return Collections.unmodifiableCollection(spawners.keySet());
        }
    }

    protected interface Handler<T extends Spawner> {

        void register(T spawner);

        void unregister(T spawner);

        void unregister(Location location);

        boolean isRegisteredSpawner(Location location);

        Collection<T> getRegistered();

        boolean contains(T spawner);

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

    public abstract Optional<Gui> getDisplayUI();

    public static <T extends Spawner> T fromItem(ItemStack itemStack, Location target) {
        throw new UnsupportedOperationException("Subclass must hide this method.");
    }


    public static abstract class ItemWrapper<T extends Spawner> {

        protected ItemWrapper() {
        }

        public abstract ItemStack toItem(T spawner);

        /**
         * Attempts to create an OfflineSpawner of the original Spawner from its itemstack component.
         * @param itemStack The itemstack to recreate to an OfflineSpawner.
         * @return Returns a populated optional of the OfflineSpawner version of the spawner, an empty
         * optional if there was any errors in re-creating the offline spawner.
         * @see #place(OfflineSpawner, Location) to place the spawner.
         */
        public abstract Optional<OfflineSpawner<T>> fromItem(ItemStack itemStack);

        public Optional<T> place(OfflineSpawner<T> offlineVersion, Location location) {
            Objects.requireNonNull(offlineVersion);
            Objects.requireNonNull(Objects.requireNonNull(location).getWorld());
            Class<? extends Spawner> clazz = offlineVersion.getClazz();
            try {
                Method method = clazz.getMethod("fromItem", ItemStack.class, Location.class);
                @SuppressWarnings("unchecked")
                T result = (T) method.invoke(null, offlineVersion.getItemStack(), location);
                result.initialize();
                return Optional.of(result);
            } catch (ReflectiveOperationException ex) {
                return Optional.empty();
            } catch (UnsupportedOperationException ex) {
                Common.log(Level.WARNING, "A an invalid spawner class was registered: " + clazz.getName());
                Common.log(Level.WARNING, ex.getMessage());
                return Optional.empty();
            }
        }

        public abstract boolean isSpawner(ItemStack itemStack);

    }
}
