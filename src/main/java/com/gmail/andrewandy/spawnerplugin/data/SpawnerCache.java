package com.gmail.andrewandy.spawnerplugin.data;

import com.gmail.andrewandy.spawnerplugin.object.Spawner;
import com.gmail.andrewandy.spawnerplugin.util.Common;
import com.gmail.andrewandy.spawnerplugin.util.cache.ConcurrentCache;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SpawnerCache extends ConcurrentCache<Spawner> {

    private final long timeoutPeriod;
    private Map<Spawner, Long> timerMap = new ConcurrentHashMap<>();

    public SpawnerCache() {
        this(TimeUnit.MINUTES, 2);
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
