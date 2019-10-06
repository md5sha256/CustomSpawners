package com.gmail.andrewandy.spawnerplugin.util.cache;

import com.gmail.andrewandy.spawnerplugin.SpawnerPlugin;
import com.gmail.andrewandy.spawnerplugin.util.Common;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Represents a thread-safe implementation of a ManagedCache, all methods below can be called async.
 */
public class ConcurrentCache<T> extends MapCache<T> implements ManagedCache<T> {

    private BukkitRunnable clearTask;
    private TimeUnit timeUnit;
    private int duration;
    private Set<String> whitelisted = ConcurrentHashMap.newKeySet(0);


    public ConcurrentCache(Function<T, String> identifier) {
        this(identifier, null);
    }

    public ConcurrentCache(Function<T, String> identifier, BukkitRunnable clearTask) {
        super(identifier, new ConcurrentHashMap<>());
        if (clearTask == null) {
            clearTask = Common.asBukkitRunnable(this::forceClear);
        }
        this.clearTask = clearTask;
    }

    public ConcurrentCache(Function<T, String> identifier, BukkitRunnable clearTask, TimeUnit timeUnit, int clearTime) {
        this(identifier, clearTask);
        if (clearTime <= 0) {
            throw new IllegalArgumentException("ClearTime must be positive!");
        }
        long timeInTicks = Common.toTicks(Objects.requireNonNull(timeUnit), clearTime);
        Common.asBukkitRunnable(clearTask).runTaskTimer(SpawnerPlugin.getInstance(), 10, timeInTicks);
    }

    @Override
    public Runnable getClearTask() {
        return clearTask;
    }

    protected void setClearTask(BukkitRunnable clearTask) {
        this.clearTask = Objects.requireNonNull(clearTask);
    }

    @Override
    public void forceClear() {
        if (clearTask == null) {
            super.clear();
        } else {
            clearTask.run();
        }
    }

    public void forceClear(boolean clearWhitelisted) {
        forceClear();
        if (clearWhitelisted) {
            this.whitelisted.clear();
        }
    }

    @Override
    public void setDelay(TimeUnit timeUnit, int duration) {
        this.timeUnit = Objects.requireNonNull(timeUnit);
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        this.duration = duration;
        this.clearTask.cancel();
        long asTicks = Common.toTicks(timeUnit, duration);
        this.clearTask.runTaskTimer(SpawnerPlugin.getInstance(), 10, asTicks);
    }

    @Override
    public long getDelay(TimeUnit timeUnit) {
        return this.timeUnit.convert(this.duration, Objects.requireNonNull(timeUnit));
    }

    public void setWhitelisted(T target, boolean whitelisted) {
        Objects.requireNonNull(target);
        setWhitelisted(getFunction().apply(target), whitelisted);
    }

    public void setWhitelisted(String identifier, boolean whitelisted) {
        Objects.requireNonNull(identifier);
        if (whitelisted) {
            this.whitelisted.add(identifier);
        } else {
            this.whitelisted.remove(identifier);
        }
    }

    public boolean isWhitelisted(T target) {
        Objects.requireNonNull(target);
        return this.whitelisted.contains(getFunction().apply(target));
    }

    public boolean isWhitelisted(String identifier) {
        Objects.requireNonNull(identifier);
        return this.whitelisted.contains(identifier);
    }
}

