package com.gmail.andrewandy.spawnerplugin.event;

import com.gmail.andrewandy.spawnerplugin.betaobjects.Spawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Objects;

public class SpawnerPlaceEvent extends Event implements SpawnerEvent {

    private static HandlerList handlers = new HandlerList();
    private final Spawner spawner;
    private Player player;
    private EquipmentSlot slot;
    private boolean cancel = false;

    public SpawnerPlaceEvent(Player player, Spawner spawner, EquipmentSlot slot) {
        this.spawner = Objects.requireNonNull(spawner);
        this.player = Objects.requireNonNull(player);
        this.slot = Objects.requireNonNull(slot);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public EquipmentSlot getSlot() {
        return slot;
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
