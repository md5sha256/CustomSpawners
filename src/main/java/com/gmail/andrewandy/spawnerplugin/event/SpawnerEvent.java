package com.gmail.andrewandy.spawnerplugin.event;

import com.gmail.andrewandy.spawnerplugin.spawner.Spawner;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Objects;

public abstract class SpawnerEvent extends Event {

    protected static HandlerList handlerList;
    private final Spawner spawner;

    public SpawnerEvent(Spawner spawner) {
        this(spawner, false);
    }

    public SpawnerEvent(Spawner spawner, boolean aysnc) {
        super(aysnc);
        this.spawner = Objects.requireNonNull(spawner);
    }

    public Spawner getSpawner() {
        return spawner;
    }
}
