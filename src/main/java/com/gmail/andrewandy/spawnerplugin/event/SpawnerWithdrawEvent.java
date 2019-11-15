package com.gmail.andrewandy.spawnerplugin.event;

import com.gmail.andrewandy.spawnerplugin.spawner.Spawner;
import com.gmail.andrewandy.spawnerplugin.spawner.stackable.StackableSpawner;
import org.bukkit.event.HandlerList;

import java.util.Objects;

public class SpawnerWithdrawEvent extends SpawnerEvent {

    private final StackableSpawner withdrawn;

    public SpawnerWithdrawEvent(StackableSpawner spawner, StackableSpawner withdrawn) {
        super(spawner);
        this.withdrawn = Objects.requireNonNull(withdrawn);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public StackableSpawner getSpawner() {
        return (StackableSpawner) super.getSpawner();
    }

    public StackableSpawner getWithdrawn() {
        return withdrawn;
    }
}
