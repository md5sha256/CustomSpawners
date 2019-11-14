package com.gmail.andrewandy.spawnerplugin.data;

import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.spawner.OfflineSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.Spawner;
import com.gmail.andrewandy.spawnerplugin.util.Common;
import com.gmail.andrewandy.spawnerplugin.util.cache.ConcurrentCache;
import com.google.common.base.Stopwatch;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class SpawnerCache extends ConcurrentCache<Spawner> {

    private final long timeoutPeriod;
    private Map<Spawner, Long> timerMap = new ConcurrentHashMap<>();
    private Listener clearTask = new Listener() {

        @EventHandler
        public void onChunkUnload(ChunkUnloadEvent event) {
            timerMap.keySet().stream().filter(spawner -> isInChunk(spawner.getLocation(), event.getChunk().getChunkSnapshot()))
                    .forEach((SpawnerCache.this::cache));
        }

        @EventHandler
        public void onChunkLoad(ChunkLoadEvent event) {
            final BlockState[] snapshot = event.getChunk().getTileEntities(true);
            SpawnerPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(SpawnerPlugin.getInstance(), () -> {
                Stopwatch stopwatch = Stopwatch.createStarted();
                double avgTime = 0;
                for (BlockState b : snapshot) {
                    if (!b.isPlaced() || !b.hasMetadata("customSpawner")) {
                        continue;
                    }
                    if (avgTime == 0) {
                        avgTime = stopwatch.elapsed(TimeUnit.NANOSECONDS);
                    } else {
                        avgTime += stopwatch.elapsed(TimeUnit.NANOSECONDS);
                        avgTime /= 2;
                    }
                    if (b.getMetadata("customSpawner").stream().anyMatch(
                            (meta) -> {
                                Plugin plugin = meta.getOwningPlugin();
                                if (plugin == null) {
                                    return false;
                                }
                                return plugin.getName().equalsIgnoreCase(SpawnerPlugin.getInstance().getName());
                            })) {
                        Material material = b.getType();
                        BlockState state = b.getBlock().getState();
                        if (!(state instanceof CreatureSpawner)) {
                            System.out.println(material);
                            System.out.println("not instance!");
                        }
                        Optional<OfflineSpawner> spawner = DataUtil.loadData(b.getLocation());
                        if (!spawner.isPresent()) {
                            return;
                        }
                        OfflineSpawner target = spawner.get();
                        Optional<Spawner> toLive = target.asSpawner(b.getLocation());
                        toLive.ifPresent(value -> cache(value));
                    }
                }
                stopwatch.stop();
                Common.log(Level.INFO, "&aTime taken: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
                Common.log(Level.INFO, "&aAvg time per spawner: " + avgTime + "ns");
            });
        }

        private boolean isInChunk(Location location, ChunkSnapshot chunk) {
            if (!location.getWorld().getName().contentEquals(chunk.getWorldName())) {
                return false;
            }
            Chunk snapshot = location.getChunk();
            return snapshot.getX() == chunk.getX() && snapshot.getZ() == chunk.getZ();
        }
    };


    public SpawnerCache() {
        super(Spawner::getIdentifier, null);
        this.timeoutPeriod = -1;
        SpawnerPlugin.getInstance().getServer().getPluginManager().registerEvents(clearTask, SpawnerPlugin.getInstance());
    }

    public SpawnerCache(TimeUnit timeUnit, int timeoutPeriod) {
        super((Spawner::getIdentifier));
        super.setClearTask(Common.asBukkitRunnable(() -> {
            for (Spawner spawner : getCached()) {
                purge(spawner);
            }
        }));
        this.timeoutPeriod = Objects.requireNonNull(timeUnit).toMillis(timeoutPeriod);
    }

    public boolean isCached(String identifier) {
        return super.getCached().stream().anyMatch((spawner) -> spawner.getIdentifier().equalsIgnoreCase(identifier));
    }

    @Override
    public void cache(Spawner spawner) {
        super.cache(spawner);
        this.timerMap.put(spawner, System.currentTimeMillis());
    }

    @Override
    public void purge(Spawner spawner) {
        super.purge(spawner);
        this.timerMap.remove(spawner);
        if (isWhitelisted(spawner) || !timeoutReached(spawner)) {
            return;
        }
        DataUtil.saveData(spawner);
        super.purge(spawner);
        this.timerMap.remove(spawner);
    }

    public long getTimeoutPeriod(TimeUnit timeUnit) {
        return TimeUnit.MILLISECONDS.convert(timeoutPeriod, Objects.requireNonNull(timeUnit));
    }

    public void resetTimeout(Spawner spawner) {
        this.timerMap.replace(spawner, System.currentTimeMillis());
    }

    public boolean timeoutReached(Spawner spawner) {
        if (!isCached(spawner)) {
            return false;
        }
        return timerMap.get(spawner) >= System.currentTimeMillis();
    }

}
