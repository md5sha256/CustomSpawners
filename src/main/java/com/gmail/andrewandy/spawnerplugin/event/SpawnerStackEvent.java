package com.gmail.andrewandy.spawnerplugin.event;

import com.gmail.andrewandy.spawnerplugin.spawner.stackable.StackableSpawner;
import org.bukkit.event.HandlerList;

public class SpawnerStackEvent extends SpawnerEvent {

    public SpawnerStackEvent(StackableSpawner stackableSpawner) {
        super(stackableSpawner);
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

}
