package com.gmail.andrewandy.spawnerplugin.event;

import com.gmail.andrewandy.spawnerplugin.spawner.Spawner;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Objects;

public abstract class SpawnerEvent extends Event {

    private final Spawner spawner;
    protected static HandlerList handlerList;

    public SpawnerEvent(Spawner spawner) {
        this.spawner = Objects.requireNonNull(spawner);
    }

    public Spawner getSpawner() {
        return spawner;
    }
}
