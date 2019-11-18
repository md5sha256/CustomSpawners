package com.gmail.andrewandy.spawnerplugin.event;

import com.gmail.andrewandy.spawnerplugin.spawner.stackable.StackableSpawner;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.Objects;

public class SpawnerWithdrawEvent extends SpawnerEvent implements Cancellable {

    private final StackableSpawner withdrawn;
    private boolean cancel;

    public SpawnerWithdrawEvent(StackableSpawner spawner, StackableSpawner withdrawn) {
        super(spawner);
        this.withdrawn = Objects.requireNonNull(withdrawn);
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

    public StackableSpawner getWithdrawn() {
        return withdrawn;
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
