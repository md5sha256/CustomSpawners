package com.gmail.andrewandy.spawnerplugin.spawner;

import com.boydti.fawe.bukkit.wrapper.AsyncWorld;
import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerLoadEvent;
import com.gmail.andrewandy.spawnerplugin.spawner.data.SpawnerData;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public interface SpawnerManager {

    BukkitTask registerSpawner(AbstractSpawner spawner);

    default void unregisterSpawner(Location spawner) {
        unregisterSpawner(spawner, false);
    }
    void unregisterSpawner(Location spawner, boolean clearData);

    Collection<Location> getRegisteredSpawners();

    Optional<AbstractSpawner> getFromLocation(Location location);

    //TODO rewrite
    default Collection<AbstractSpawner> getFromChunk(ChunkSnapshot snapshot, boolean async) {
        Collection<AbstractSpawner> target = new LinkedList<>();
        Runnable runnable = () -> {
            World world = Bukkit.getWorld(snapshot.getWorldName());
            if (world == null) {
                throw new IllegalArgumentException("No world found.");
            }
            for (int y = 0; y < 254; y++) {
                for (int x = snapshot.getX(); x < snapshot.getX() + 16; x++) {
                    for (int z = snapshot.getZ(); z < snapshot.getZ() + 16; z++) {
                        Location location = new Location(world, x, y, z);
                        if (!SpawnerData.isRegistered(location)) {
                            continue;
                        }
                        Optional<AbstractSpawner> optionalSpawner = getFromLocation(location);
                        if (!optionalSpawner.isPresent()) {
                            continue;
                        }
                        AbstractSpawner spawner = optionalSpawner.get();
                        new SpawnerLoadEvent(spawner, async).callEvent();
                    }
                }
            }
        };
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(SpawnerPlugin.getInstance(), runnable);
        } else {
            runnable.run();
        }
        return target;
    }

    default void saveAll() {
        getRegisteredSpawners().forEach(this::unregisterSpawner);
    }

    /*
     * Loads all the given spawners in all worlds. This method will attempt to load spawners
     * async as much as possible but is still quite a heavy task.
     *
     * @param plugin The plugin to the run the task as.
     */
    default void loadAllSpawners(JavaPlugin plugin) {
        Objects.requireNonNull(plugin);
        Runnable runnable = () -> {
            for (World world : Bukkit.getWorlds()) {
                Runnable worldLoad = () -> {
                    Chunk[] loaded = world.getLoadedChunks();
                    ChunkSnapshot[] snapshots = new ChunkSnapshot[loaded.length];
                    System.out.println(loaded.length);
                    System.out.println(loaded.length * 256);
                    for (int i = 0; i < snapshots.length; i++) {
                        snapshots[i] = loaded[i].getChunkSnapshot();
                    }
                    Collection<AbstractSpawner> spawners = new LinkedList<>();
                    Collection<BukkitTask> tasks = new HashSet<>(spawners.size(), 1.00F);
                    for (ChunkSnapshot snapshot : snapshots) {
                        BukkitRunnable task = new BukkitRunnable() {
                            @Override
                            public void run() {
                                Collection<AbstractSpawner> temp = getFromChunk(snapshot, false);
                                synchronized (spawners) {
                                    spawners.addAll(temp);
                                }
                                this.cancel();
                            }
                        };
                        tasks.add(task.runTaskAsynchronously(plugin));
                    }
                    while (!tasks.isEmpty()) {
                        tasks.removeIf(BukkitTask::isCancelled);
                    }
                    Runnable task = () -> {
                        synchronized (Spawners.defaultManager()) {
                            spawners.forEach(spawner -> {
                                spawner.registerToManager(Spawners.defaultManager());
                                new SpawnerLoadEvent(spawner, true).callEvent();
                            });
                        }
                    };
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
                };
                Bukkit.getScheduler().runTaskAsynchronously(plugin, worldLoad);
            }
        };
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

}
