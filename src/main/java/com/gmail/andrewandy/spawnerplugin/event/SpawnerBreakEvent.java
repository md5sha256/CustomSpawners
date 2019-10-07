package com.gmail.andrewandy.spawnerplugin.event;

import com.gmail.andrewandy.spawnerplugin.object.Spawner;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Objects;

public class SpawnerBreakEvent extends Event implements SpawnerEvent, Cancellable {

    private static HandlerList handlers = new HandlerList();
    private Entity breaker;
    private final Spawner spawner;
    private boolean cancel = false;

    public SpawnerBreakEvent(Entity breaker, Spawner spawner) {
        this.spawner = Objects.requireNonNull(spawner);
        this.breaker = Objects.requireNonNull(breaker);
    }

    public Entity getBreaker() {
        return breaker;
    }

    @Override
    public Spawner getSpawner() {
        return spawner;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancel = b;
    }
}
