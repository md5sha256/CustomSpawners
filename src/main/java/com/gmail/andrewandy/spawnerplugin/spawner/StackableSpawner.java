package com.gmail.andrewandy.spawnerplugin.spawner;

import java.util.Collection;
import java.util.Objects;

public interface StackableSpawner extends Spawner {

    Collection<? extends StackableSpawner> getStacked();

    void stack(OfflineSpawner<? extends StackableSpawner> spawner);

    boolean canStack(OfflineSpawner<? extends StackableSpawner> spawner);

    boolean canStack(Class<? extends StackableSpawner> clazz);

    default <T extends OfflineSpawner<? extends StackableSpawner>> boolean canStack(Collection<T> spawners) {
        Objects.requireNonNull(spawners);
        for (T spawner : spawners) {
            if (!canStack(spawner)) {
                return false;
            }
        }
        return true;
    }

    void remove(OfflineSpawner<? extends StackableSpawner> spawner);

    <T extends StackableSpawner> Collection<T> removeAll(Class<OfflineSpawner<T>> clazz);

    /**
     * Attempts to stack all of the spawners specified.
     *
     * @param spawners     The collection of spawners to attempt to be stacked.
     * @param <T>          The type of Spawners to be stacked.
     * @param ignoreOnFail Whether to account for failed merges.
     * @return Returns the result of the operation, true if all spawners were stacked, or ignoreOnFail is true.
     * False if the spawners were unable to be stacked, if this is the case sub classes should revert changes
     * meaning, no spawners were stacked.
     */
    <T extends OfflineSpawner<? extends StackableSpawner>> boolean stackAll(Collection<T> spawners, boolean ignoreOnFail);
}
