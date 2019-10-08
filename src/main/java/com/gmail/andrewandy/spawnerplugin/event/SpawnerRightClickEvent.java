package com.gmail.andrewandy.spawnerplugin.event;

import com.gmail.andrewandy.spawnerplugin.object.Spawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class SpawnerRightClickEvent extends Event implements SpawnerEvent {
    private static HandlerList handlers = new HandlerList();
    private Player player;
    private Spawner spawner;
    private boolean cancel;

    public SpawnerRightClickEvent(Player player, Spawner spawner) {
        this.player = player;
        this.spawner = spawner;
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

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
