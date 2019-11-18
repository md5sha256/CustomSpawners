package com.gmail.andrewandy.spawnerplugin.event;

import com.gmail.andrewandy.spawnerplugin.spawner.stackable.StackableSpawner;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class SpawnerStackEvent extends SpawnerEvent implements Cancellable {

    private boolean cancel;

    public SpawnerStackEvent(StackableSpawner stackableSpawner) {
        super(stackableSpawner);
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    @Override
    public StackableSpawner getSpawner() {
        return (StackableSpawner) super.getSpawner();
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
