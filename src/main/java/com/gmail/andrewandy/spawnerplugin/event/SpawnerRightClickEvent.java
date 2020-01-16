package com.gmail.andrewandy.spawnerplugin.event;

import com.gmail.andrewandy.spawnerplugin.spawner.AbstractSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Objects;

public class SpawnerRightClickEvent extends SpawnerEvent implements Cancellable {

    private Entity clicker;
    private boolean cancel;
    private ItemStack itemInHand;

    public SpawnerRightClickEvent(AbstractSpawner spawner, Entity clicker, ItemStack inHand) {
        super(spawner);
        this.clicker = Objects.requireNonNull(clicker);
        this.itemInHand = inHand;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Nullable
    public ItemStack getItemInHand() {
        return itemInHand;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public Entity getClicker() {
        return clicker;
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
