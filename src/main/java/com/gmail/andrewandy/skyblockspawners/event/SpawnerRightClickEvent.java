package com.gmail.andrewandy.skyblockspawners.event;

import com.gmail.andrewandy.skyblockspawners.object.Spawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class SpawnerRightClickEvent extends Event {
    private static HandlerList handlers = new HandlerList();
    private Player player;
    private Spawner spawner;

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

    public Spawner getSpawner() {
        return spawner;
    }
}
