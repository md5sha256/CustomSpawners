package com.gmail.andrewandy.spawnerplugin.betaobjects;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StackableSpawner {

    int getSize();

    int getMaxSize();

    void add(OfflineSpawner spawner);

    void addAll(Collection<OfflineSpawner> spawners);

    void withdraw(OfflineSpawner spawner);

    void clear();

    default boolean isFull() {
        return getSize() == getMaxSize();
    }

    default boolean isStacked(OfflineSpawner spawner) {
        return getStacked().stream()
                .anyMatch((spawner1 -> spawner1.getSpawnerID().equals(spawner.getSpawnerID())));
    }

    default boolean isStacked(UUID spawnerID) {
        return getStacked().stream().anyMatch(spawner -> spawner.getSpawnerID().equals(spawnerID));
    }

    default Optional<OfflineSpawner> getById(UUID spawnerID) {
        return getStacked().stream()
                .filter((spawner -> spawner.getSpawnerID().equals(spawnerID)))
                .findFirst();
    }

    boolean canStack(OfflineSpawner spawner);

    List<OfflineSpawner> getStacked();

}
