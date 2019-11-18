package com.gmail.andrewandy.spawnerplugin.listener;

import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerLoadEvent;
import com.gmail.andrewandy.spawnerplugin.event.SpawnerUnloadEvent;
import com.gmail.andrewandy.spawnerplugin.spawner.AbstractSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.Spawners;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Collection;

public class ChunkListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onChunkLoad(ChunkLoadEvent event) {
        final ChunkSnapshot snapshot = event.getChunk().getChunkSnapshot();
        Runnable task = () -> {
            synchronized (Spawners.defaultManager()) {
                Collection<AbstractSpawner> spawners = Spawners.defaultManager().getFromChunk(snapshot, false);
                spawners.forEach(spawner -> {
                    spawner.register();
                    new SpawnerLoadEvent(spawner, true).callEvent();
                });
            }
        };
        Bukkit.getScheduler().runTaskAsynchronously(SpawnerPlugin.getInstance(), task);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        final ChunkSnapshot snapshot = event.getChunk().getChunkSnapshot();
        Runnable task = () -> {
            synchronized (Spawners.defaultManager()) {
                Collection<AbstractSpawner> spawners = Spawners.defaultManager().getFromChunk(snapshot, false);
                spawners.forEach(spawner -> {
                    SpawnerUnloadEvent spawnerEvent = new SpawnerUnloadEvent(spawner, true);
                    if (!spawnerEvent.isCancelled()) {
                        spawner.unregister();
                    }
                });
            }
        };
        Bukkit.getScheduler().runTaskAsynchronously(SpawnerPlugin.getInstance(), task);
    }
}
