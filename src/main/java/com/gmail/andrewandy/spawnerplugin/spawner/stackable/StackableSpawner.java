package com.gmail.andrewandy.spawnerplugin.spawner.stackable;

import com.gmail.andrewandy.spawnerplugin.spawner.OfflineSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.Spawner;

import java.util.Collection;
import java.util.Objects;

public interface StackableSpawner<T extends StackableSpawner> extends Spawner {

    Collection<OfflineSpawner<T>> getStacked();

    void stack(OfflineSpawner<T> spawner);

    default boolean canStack(Collection<OfflineSpawner<T>> spawners) {
        Objects.requireNonNull(spawners);
        return spawners.size() + size() > maxSize();
    }

    void remove(OfflineSpawner<T> spawner);

    int maxSize();

    default int size() {
        return getStacked().size();
    }

    default boolean isFull() {
        return size() < maxSize();
    }

    /**
     * Attempts to stack all of the spawners specified.
     *
     * @param spawners The collection of spawners to attempt to be stacked.
     * @return Returns the result of the operation, true if all spawners were stacked, or ignoreOnFail is true.
     * False if the spawners were unable to be stacked, if this is the case sub classes should revert changes
     * meaning, no spawners were stacked.
     */
    boolean stackAll(Collection<OfflineSpawner<T>> spawners);
}
