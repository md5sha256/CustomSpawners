package com.gmail.andrewandy.spawnerplugin.spawner;

import com.gmail.andrewandy.spawnerplugin.event.SpawnerLoadEvent;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public interface SpawnerManager {

    BukkitTask registerSpawner(AbstractSpawner spawner);

    void unregisterSpawner(Location spawner);

    Collection<Location> getRegisteredSpawners();

    Optional<AbstractSpawner> getFromLocation(Location location);

    default Collection<AbstractSpawner> getFromChunk(ChunkSnapshot snapshot) {
        Collection<AbstractSpawner> target = new LinkedList<>();
        for (int i = 0; i < 256; i++) {
            if (snapshot.isSectionEmpty(i)) {
                continue;
            }
            //TODO check blocks.
        }
        return target;
    }

    default void saveAll() {
        getRegisteredSpawners().forEach(this::unregisterSpawner);
    }

    /**
     * Loads all the given spawners in all worlds. This method will attempt to load spawners
     * async as much as possible but is still quite a heavy task.
     *
     * @param plugin The plugin to the run the task as.
     */
    default void loadAllSpawners(JavaPlugin plugin) {
        Objects.requireNonNull(plugin);
        for (World world : Bukkit.getWorlds()) {
            Chunk[] loaded = world.getLoadedChunks();
            ChunkSnapshot[] snapshots = new ChunkSnapshot[loaded.length];
            for (int i = 0; i < snapshots.length; i++) {
                snapshots[i] = loaded[i].getChunkSnapshot();
            }
            Collection<AbstractSpawner> spawners = new LinkedList<>();
            Collection<BukkitTask> tasks = new HashSet<>(spawners.size(), 1.00F);
            for (ChunkSnapshot snapshot : snapshots) {
                BukkitRunnable task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        Collection<AbstractSpawner> temp = getFromChunk(snapshot);
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
        }
    }

}
