package com.gmail.andrewandy.spawnerplugin.object;

import java.util.Collection;

public interface Stackable<T> {

    boolean isFull();
    int getMaxSize();
    default int getSize() {
        return getStacked().size();
    }
    boolean stackEligible(Object o);
    Collection<OfflineSpawner> getStacked();
    void stack(OfflineSpawner offlineSpawner);
    void withdraw(OfflineSpawner offlineSpawner);
    void stack(T stackable);
    void clearStack();
}
