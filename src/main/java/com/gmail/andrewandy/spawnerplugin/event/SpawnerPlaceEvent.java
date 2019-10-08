package com.gmail.andrewandy.spawnerplugin.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Objects;

public class SpawnerPlaceEvent extends Event implements SpawnerEvent {

    private static HandlerList handlers = new HandlerList();
    private final Spawner spawner;
    private Player player;

    public SpawnerPlaceEvent(Player player, Spawner spawner) {
        this.spawner = Objects.requireNonNull(spawner);
        this.player = Objects.requireNonNull(player);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public Spawner getSpawner() {
        return spawner;
    }
}
