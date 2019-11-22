package com.gmail.andrewandy.spawnerplugin.spawner;

import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerLoadEvent;
import com.gmail.andrewandy.spawnerplugin.spawner.data.SpawnerData;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;

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
    default Collection<AbstractSpawner> getFromChunk(ChunkSnapshot snapshot, boolean async, boolean deep) {
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
                        if (!SpawnerData.isRegistered(location) && !deep) {
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

    /**
     * Loads all the given spawners in all worlds. This method will attempt to load spawners
     * async as much as possible but is still quite a heavy task.
     *
     * @param plugin The plugin to the run the task as.
     */
    default BukkitTask loadAllSpawners(JavaPlugin plugin) {
        Objects.requireNonNull(plugin);
        Collection<? extends Player> players = new HashSet<>(Bukkit.getOnlinePlayers());
        //Get players
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : players) {
                    Runnable task = () -> {
                        int viewDistance = player.getViewDistance();
                        Chunk current = player.getChunk();
                        //'Top left position'
                        Location pos1 = new Location(player.getWorld(), current.getX() + (16 * viewDistance + 1), 0, current.getZ() + (16 * viewDistance));
                        //'Bottom right position'
                        Location pos2 = new Location(player.getWorld(), current.getX() - (16 * viewDistance), 255, current.getZ() - (16 * viewDistance + 1));
                        BoundingBox boundingBox = new BoundingBox(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
                        for (Location location : getRegisteredSpawners()) {
                            if (!boundingBox.contains(location.getX(), location.getY(), location.getZ())) {
                                continue;
                            }
                            //If location is registered...
                            Optional<AbstractSpawner> optional = getFromLocation(location);
                            if (!optional.isPresent()) {
                                unregisterSpawner(location);
                                continue;
                            }
                            //Register the spawner.
                            registerSpawner(optional.get());
                        }
                    };
                    Bukkit.getScheduler().runTask(plugin, task);
                }
                this.cancel();
            }
        };
        //Async looping, but at the end of the day the actual processing in done back on the main thread.
        return runnable.runTaskAsynchronously(plugin);
    }
}
