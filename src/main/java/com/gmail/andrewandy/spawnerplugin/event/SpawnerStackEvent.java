package com.gmail.andrewandy.spawnerplugin.event;

import com.gmail.andrewandy.spawnerplugin.spawner.OfflineSpawner;
import com.gmail.andrewandy.spawnerplugin.spawner.stackable.StackableSpawner;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.Objects;

public class SpawnerStackEvent extends SpawnerEvent implements Cancellable {

    private boolean cancel;
    private OfflineSpawner offlineSpawner;

    public SpawnerStackEvent(StackableSpawner stackableSpawner, OfflineSpawner offlineSpawner) {
        super(stackableSpawner);
        this.offlineSpawner = Objects.requireNonNull(offlineSpawner);
    }

    public OfflineSpawner getOfflineSpawner() {
        return offlineSpawner;
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
